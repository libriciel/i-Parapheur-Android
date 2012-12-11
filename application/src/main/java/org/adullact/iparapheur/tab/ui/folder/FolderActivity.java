package org.adullact.iparapheur.tab.ui.folder;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.inject.Inject;
import java.util.Collections;
import org.adullact.iparapheur.tab.IParapheurTabException;
import org.adullact.iparapheur.tab.R;
import org.adullact.iparapheur.tab.model.AbstractFolderFile;
import org.adullact.iparapheur.tab.model.Folder;
import org.adullact.iparapheur.tab.model.FolderRequestedAction;
import com.artifex.mupdf.MuPDFActivity;
import com.artifex.mupdf.MuPDFCore;
import com.artifex.mupdf.MuPDFPageAdapter;
import com.artifex.mupdf.MuPDFPageView;
import com.artifex.mupdf.PageView;
import com.artifex.mupdf.ReaderView;
import org.adullact.iparapheur.tab.services.AccountsRepository;
import org.adullact.iparapheur.tab.services.IParapheurHttpClient;
import org.adullact.iparapheur.tab.ui.Refreshable;
import org.adullact.iparapheur.tab.ui.actionbar.ActionBarActivityObserver;
import org.adullact.iparapheur.tab.ui.actions.ActionsDialogFactory;
import org.adullact.iparapheur.tab.ui.dashboard.DashboardActivity;
import org.adullact.iparapheur.tab.ui.folder.FolderFileListFragment.FolderListAdapter;
import org.adullact.iparapheur.tab.ui.folder.FolderFileListFragment.OnFileDisplayRequestListener;
import org.adullact.iparapheur.tab.ui.office.OfficeActivity;
import org.codeartisans.android.toolbox.activity.RoboFragmentActivity;
import org.codeartisans.android.toolbox.app.UserErrorDialogFactory;
import org.codeartisans.android.toolbox.logging.AndrologInitOnCreateObserver;
import org.codeartisans.android.toolbox.os.AsyncTaskResult;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.AutoQuotaGrowWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.ChainedWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebChromeSupport.ConsoleAndrologWebChromeClient;
import org.codeartisans.android.toolbox.webkit.WebViewSupport.JSInjectWebViewClient;
import org.codeartisans.java.toolbox.Strings;
import org.json.JSONObject;
import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;

import de.akquinet.android.androlog.Log;

public class FolderActivity
        extends RoboFragmentActivity
        implements Refreshable {

    public static final String EXTRA_ACCOUNT_IDENTITY = "account:identity";
    public static final String EXTRA_OFFICE_IDENTITY = "office:identity";
    public static final String EXTRA_OFFICE_TITLE = "office:title";
    public static final String EXTRA_FOLDER_IDENTITY = "folder:identity";
    public static final String EXTRA_FOLDER_TITLE = "folder:title";
    @Inject
    private AndrologInitOnCreateObserver andrologInitOnCreateObserver;
    @Inject
    private ActionBarActivityObserver actionBarObserver;
    @Inject
    private AccountsRepository accountsRepository;
    @Inject
    private IParapheurHttpClient iParapheurClient;
    @Inject
    private ActionsDialogFactory actionsDialogFactory;
    @InjectFragment( R.id.folder_list_fragment)
    private FolderFileListFragment folderFileListFragment;
    @InjectView( R.id.folder_top_title)
    private TextView title;
    @InjectView( R.id.folder_top_summary_left)
    private TextView topSummaryLeft;
    @InjectView( R.id.folder_top_summary_right)
    private TextView topSummaryRight;
    @InjectView( R.id.folder_button_positive)
    private Button positiveButton;
    @InjectView( R.id.folder_button_negative)
    private Button negativeButton;
    
    private ReaderView fileWebView;
    private Folder currentFolder;
    private String accountIdentity;
    private String folderIdentity;
    private String officeIdentity;
    private MuPDFCore core;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gather Intent Extras
        accountIdentity = getIntent().getExtras().getString(EXTRA_ACCOUNT_IDENTITY);
        String officeTitle = getIntent().getExtras().getString(EXTRA_OFFICE_TITLE);
        officeIdentity = getIntent().getExtras().getString(EXTRA_OFFICE_IDENTITY);
        String folderTitle = getIntent().getExtras().getString(EXTRA_FOLDER_TITLE);
        folderIdentity = getIntent().getExtras().getString(EXTRA_FOLDER_IDENTITY);

        // Setup Activity
        setTitle(officeTitle + " > " + folderTitle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.folder);

        // Set title
        title.setText(folderTitle);

        // Create button listeners
        final Intent successIntent = new Intent(FolderActivity.this, OfficeActivity.class);
        successIntent.putExtra(OfficeActivity.EXTRA_ACCOUNT_IDENTITY, accountIdentity);
        successIntent.putExtra(OfficeActivity.EXTRA_OFFICE_TITLE, officeTitle);
        successIntent.putExtra(OfficeActivity.EXTRA_OFFICE_IDENTITY, officeIdentity);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                actionsDialogFactory.buildActionDialog(accountIdentity, officeIdentity, Collections.singletonList(currentFolder), successIntent).show();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                actionsDialogFactory.buildRejectDialog(accountIdentity, officeIdentity, Collections.singletonList(currentFolder), successIntent).show();
            }
        });

        refresh();
    }

    public void refresh() {
        // Clear View
        topSummaryLeft.setText(Strings.EMPTY);
        topSummaryRight.setText(Strings.EMPTY);
        positiveButton.setVisibility(View.INVISIBLE);
        negativeButton.setVisibility(View.INVISIBLE);

        // Load Folder
        new FolderLoadingTask(this, accountsRepository, iParapheurClient) {
            @Override
            protected void beforeDialogDismiss(AsyncTaskResult<Folder, IParapheurTabException> result) {
                if (result.getResult() != null) {

                    // Update view state
                    Folder folder = result.getResult();
                    if (folder.getRequestedAction() != FolderRequestedAction.UNSUPPORTED) {
                        positiveButton.setVisibility(View.VISIBLE);
                        negativeButton.setVisibility(View.VISIBLE);
                    }
                    switch (folder.getRequestedAction()) {
                        case SIGNATURE:
                            positiveButton.setText(getResources().getString(R.string.actions_sign));
                            break;
                        case VISA:
                            positiveButton.setText(getResources().getString(R.string.actions_visa));
                            break;
                    }
                    topSummaryLeft.setText(Html.fromHtml("<b>" + folder.getBusinessType() + "</b><br/>" + folder.getBusinessSubType()));
                    StringBuilder summaryRight = new StringBuilder();
                    summaryRight.append("<b>").append(getResources().getString(R.string.folder_created_at)).
                            append("</b> ").append(folder.getDisplayCreationDate());
                    if (folder.getDueDate() != null) {
                        summaryRight.append("<br/><b>").append(getResources().getString(R.string.folder_todo_before)).
                                append("</b> ").append(folder.getDisplayDueDate());
                    }
                    topSummaryRight.setText(Html.fromHtml(summaryRight.toString()));
                    folderFileListFragment.setListAdapter(new FolderListAdapter(context, folder.getAllFiles()));
                    folderFileListFragment.setOnFileDisplayRequestListener(new OnFileDisplayRequestListener() {
                        public void onFileDisplayRequest(AbstractFolderFile file) {
                            displayFile(file);
                        }
                    });
                    if (!folder.getAllFiles().isEmpty()) {
                        displayFile(folder.getAllFiles().get(0));
                    }
                    currentFolder = folder;

                } else {
                    currentFolder = null;
                }

            }
            private DialogInterface.OnClickListener refresh = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    refresh();
                }
            };
            private DialogInterface.OnClickListener dashboard = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(context, DashboardActivity.class));
                }
            };

            @Override
            protected void afterDialogDismiss(AsyncTaskResult<Folder, IParapheurTabException> result) {
                if (result.hasError()) {
                    UserErrorDialogFactory.show(context, getResources().getString(R.string.folder_loading_error),
                            result.getErrors(),
                            getResources().getString(R.string.words_retry), refresh,
                            getResources().getString(R.string.dashboard), dashboard);
                }
            }
        }.execute(new FolderLoadingTask.Params(accountIdentity, folderIdentity, officeIdentity));
    }

    private void displayFile(AbstractFolderFile file) {
        fileWebView = new ReaderView(this);
        Log.d("File URL : " + file.getUrl());
        folderFileListFragment.shadeFiles(file);
        JSONObject json = file.getPageImagesJSON();

        try {
            core = new MuPDFCore("/mnt/sdcard/Download/2test.pdf");
        } catch (Exception e) {
            Log.d("Exception catched : " + e.getMessage());
        }

        fileWebView.setAdapter(new MuPDFPageAdapter(this, core));
        LinearLayout layout = (LinearLayout) this.findViewById(R.id.folder_details_webview);
        layout.addView(fileWebView);
        layout.invalidate();
        //setContentView(fileWebView);
    }
}
