package org.adullact.iparapheur.controller.rest.mapper;

import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import org.adullact.iparapheur.model.Action;
import org.adullact.iparapheur.model.Annotation;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.model.Document;
import org.adullact.iparapheur.model.Dossier;
import org.adullact.iparapheur.model.EtapeCircuit;
import org.adullact.iparapheur.model.PageAnnotations;
import org.adullact.iparapheur.model.RequestResponse;
import org.adullact.iparapheur.model.SignInfo;
import org.adullact.iparapheur.utils.JsonExplorer;
import org.adullact.iparapheur.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;


public class ModelMapper {

	protected static String DOSSIER_ID = "id";
	protected static String DOSSIER_TITLE = "title";
	protected static String DOSSIER_TYPE = "type";
	protected static String DOSSIER_SUBTYPE = "sousType";
	protected static String DOSSIER_ACTION_DEMANDEE = "actionDemandee";
	protected static String DOSSIER_EMISSION_DATE = "dateEmission";
	protected static String DOSSIER_DATE_LIMITE = "dateLimite";
	protected static String DOSSIER_DOCUMENTS = "documents";
	protected static String DOSSIER_CIRCUIT = "circuit";

	protected static String DOCUMENT_ID = "id";
	protected static String DOCUMENT_NAME = "name";
	protected static String DOCUMENT_VISUEL_PDF = "visuelPdf";
	protected static String DOCUMENT_SIZE = "size";
	protected static String DOCUMENT_IS_LOCKED = "isLocked";
	protected static String DOCUMENT_IS_MAIN_DOCUMENT = "isMainDocument";

	protected static String CIRCUIT_IS_DIGITAL_SIGNATURE_MANDATORY = "isDigitalSignatureMandatory";
	protected static String CIRCUIT_HAS_SELECTION_SCRIPT = "hasSelectionScript";
	protected static String CIRCUIT_SIG_FORMAT = "sigFormat";
	protected static String CIRCUIT_ETAPES = "etapes";

	protected static String CIRCUIT_ETAPES_DATE_VALIDATION = "dateValidation";
	protected static String CIRCUIT_ETAPES_APPROVED = "approved";
	protected static String CIRCUIT_ETAPES_REJECTED = "rejected";
	protected static String CIRCUIT_ETAPES_PARAPHEUR_NAME = "parapheurName";
	protected static String CIRCUIT_ETAPES_SIGNATAIRE = "signataire";
	protected static String CIRCUIT_ETAPES_ACTION_DEMANDEE = "actionDemandee";
	protected static String CIRCUIT_ETAPES_PUBLIC_ANNOTATIONS = "annotPub";

	protected static String SIGN_INFO_SIGNATURE_INFORMATIONS = "signatureInformations";
	protected static String SIGN_INFO_HASH = "hash";

	public Dossier getDossier(RequestResponse requestResponse) throws RuntimeException {
		Dossier dossier = null;

		if (requestResponse.getResponse() != null)
			dossier = getDossier(requestResponse.getResponse());

		return dossier;
	}

	protected Dossier getDossier(JSONObject jsonObject) {

		String dossierRef = jsonObject.optString("dossierRef");
		if (dossierRef.contains("workspace://SpacesStore/")) {
			dossierRef = dossierRef.substring("workspace://SpacesStore/".length());
		}
		ArrayList<Action> actions = getActionsForDossier(jsonObject);
		Dossier dossier = new Dossier(
				dossierRef,
				jsonObject.optString("titre"),
				Action.valueOf(jsonObject.optString("actionDemandee", "VISA")),
				actions,
				jsonObject.optString("type"),
				jsonObject.optString("sousType"),
				StringUtils.parseISO8601Date(jsonObject.optString("dateCreation")),
				StringUtils.parseISO8601Date(jsonObject.optString("dateLimite"))
		);

		JSONArray documents = jsonObject.optJSONArray("documents");
		if (documents != null) {

			for (int index = 0; index < documents.length(); index++) {
				JSONObject doc = documents.optJSONObject(index);

				if (doc != null) {
					String docRef = jsonObject.optString("dossierRef");
					if (docRef.contains("workspace://SpacesStore/"))
						docRef = dossierRef.substring("workspace://SpacesStore/".length());

					String downloadUrl = doc.optString("downloadUrl");
					if (doc.has("visuelPdfUrl")) {
						downloadUrl += ";ph:visuel-pdf";
					}

					dossier.addDocument(
							new Document(
									docRef, dossierRef, doc.optString("name"), doc.optInt("size", -1), downloadUrl, false, (index == 0)
							)
					);
				}
			}
		}
		return dossier;
	}

	protected ArrayList<Action> getActionsForDossier(JSONObject dossier) {
		ArrayList<Action> actions = new ArrayList<Action>();
		boolean isActeurCourant = dossier.optBoolean("isActeurCourant", false);
		if (isActeurCourant) {
			actions.add(Action.EMAIL);
			actions.add(Action.JOURNAL);
			actions.add(Action.ENREGISTRER);
		}
		JSONObject returnedActions = dossier.optJSONObject("actions");
		String actionDemandee = dossier.optString("actionDemandee");
		if (returnedActions != null) {
			Iterator actionsIterator = returnedActions.keys();
			while (actionsIterator.hasNext()) {
				String action = (String) actionsIterator.next();
				boolean isActionEnabled = returnedActions.optBoolean(action, false);
				if (isActionEnabled) {
					if (action.equals("archive")) {
						if (actionDemandee.equals("ARCHIVAGE")) {
							actions.add(Action.ARCHIVAGE);
						}
					}
					else if (action.equals("delete")) {
						actions.add(Action.SUPPRESSION);
					}
					else if (action.equals("reject")) {
						actions.add(Action.REJET);
					}
					else if (action.equals("remorse")) {
						actions.add(Action.REMORD);
					}
					else if (action.equals("secretary")) {
						actions.add(Action.SECRETARIAT);
					}
					else if (action.equals("sign")) {
						if (actionDemandee.equals("SIGNATURE")) {
							actions.add(Action.SIGNATURE);
						}
						else if (actionDemandee.equals("VISA")) {
							actions.add(Action.VISA);
						}
						else if (actionDemandee.equals("MAILSEC")) {
							actions.add(Action.MAILSEC);
						}
					}
				}
			}
		}
		return actions;
	}

	public ArrayList<Dossier> getDossiers(RequestResponse requestResponse) {
		ArrayList<Dossier> dossiers = new ArrayList<>();
		if (requestResponse.getResponse() != null) {
			JSONArray array = requestResponse.getResponse().optJSONArray("dossiers");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject dossierJSON = array.optJSONObject(i);
					if (dossierJSON != null) {
						Dossier dossier = getDossier(dossierJSON);
						if (dossier != null) {
							dossiers.add(dossier);
						}
					}
				}
			}
		}
		return dossiers;
	}

	public @NonNull ArrayList<EtapeCircuit> getCircuit(@NonNull RequestResponse response) {

		ArrayList<EtapeCircuit> circuit = new ArrayList<>();
		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());

		for (int index = 0; index < jsonExplorer.findArray("circuit").getCurrentArraySize(); index++) {

			EtapeCircuit etapeCircuit = new EtapeCircuit(
					jsonExplorer.findArray("circuit").find(index).optString("dateValidation"),
					jsonExplorer.findArray("circuit").find(index).optBoolean("approved", false),
					jsonExplorer.findArray("circuit").find(index).optBoolean("rejected", false),
					jsonExplorer.findArray("circuit").find(index).optString("parapheurName", ""),
					jsonExplorer.findArray("circuit").find(index).optString("signataire", ""),
					jsonExplorer.findArray("circuit").find(index).optString("actionDemandee", Action.VISA.toString()),
					jsonExplorer.findArray("circuit").find(index).optString("annotPub", "")
			);

			circuit.add(etapeCircuit);
		}

		return circuit;
	}

	public ArrayList<Bureau> getBureaux(RequestResponse response) {
		ArrayList<Bureau> bureaux = new ArrayList<Bureau>();
		if (response.getResponse() != null) {
			JSONArray array = response.getResponse().optJSONArray("bureaux");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject bureau = array.optJSONObject(i);
					if (bureau != null) {
						String bureauRef = bureau.optString("nodeRef");
						if (bureauRef.contains("workspace://SpacesStore/")) {
							bureauRef = bureauRef.substring("workspace://SpacesStore/".length());
						}
						bureaux.add(new Bureau(bureauRef, bureau.optString("name")));
					}
				}
			}
		}
		return bureaux;
	}

	public @NonNull SignInfo getSignInfo(RequestResponse response) {
		SignInfo result = new SignInfo();

		JsonExplorer jsonExplorer = new JsonExplorer(response.getResponse());
		String hash = jsonExplorer.findObject(SIGN_INFO_SIGNATURE_INFORMATIONS).optString(SIGN_INFO_HASH, null);

		result.setHash(hash);

		return result;
	}

	public LinkedHashMap<String, ArrayList<String>> getTypologie(RequestResponse response) {
		LinkedHashMap<String, ArrayList<String>> typologie = new LinkedHashMap<String, ArrayList<String>>();
		if (response.getResponse() != null) {
			JSONObject data = response.getResponse().optJSONObject("data");
			if (data != null) {
				JSONObject typology = data.optJSONObject("typology");
				if (typology != null) {
					Iterator types = typology.keys();
					while (types.hasNext()) {
						String type = (String) types.next();
						JSONArray jsonSousTypes = typology.optJSONArray(type);
						if (jsonSousTypes != null) {
							ArrayList<String> sousTypes = new ArrayList<String>(jsonSousTypes.length());
							for (int i = 0; i < jsonSousTypes.length(); i++) {
								String sousType = jsonSousTypes.optString(i);
								if (!sousType.isEmpty()) {
									sousTypes.add(sousType);
								}
							}
							typologie.put(type, sousTypes);
						}
					}
				}
			}
		}
		return typologie;
	}

	public SparseArray<PageAnnotations> getAnnotations(RequestResponse response) {
		SparseArray<PageAnnotations> annotations = new SparseArray<>();

		if (response.getResponseArray() == null)
			return annotations;

		JSONArray etapes = response.getResponseArray();
		for (int step = 0; step < etapes.length(); step++) {
			JSONObject etapeAnnotations = etapes.optJSONObject(step);
			if (etapeAnnotations != null) {
				Iterator pages = etapeAnnotations.keys();
				while (pages.hasNext()) {
					String pageStr = (String) pages.next();
					JSONArray pagesAnnotations = etapeAnnotations.optJSONArray(pageStr);
					if (pagesAnnotations != null) {
						PageAnnotations pageAnnotations = new PageAnnotations();
						for (int page = 0; page < pagesAnnotations.length(); page++) {
							JSONObject jsonAnnotation = pagesAnnotations.optJSONObject(page);
							if (jsonAnnotation != null) {

								try {
									Date date = null;
									try {
										date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US).parse(
												jsonAnnotation.getString("date")
										);
									}
									catch (ParseException e) {
										date = new Date();
									}
									catch (JSONException e) {
										date = new Date();
									}
									String outDate = new SimpleDateFormat("dd/MM/yyyy' - 'HH:mm").format(date);

									JSONObject jsonRect = jsonAnnotation.getJSONObject("rect");
									JSONObject topLeft = jsonRect.getJSONObject("topLeft");
									JSONObject bottomRight = jsonRect.getJSONObject("bottomRight");
									RectF rect = new RectF(
											topLeft.getLong("x"), topLeft.getLong("y"), bottomRight.getLong("x"), bottomRight.getLong("y")
									);

									pageAnnotations.add(
											new Annotation(
													jsonAnnotation.optString("id"),
													jsonAnnotation.optString("author"),
													Integer.parseInt(pageStr),
													jsonAnnotation.optBoolean("secretaire"),
													outDate,
													rect,
													jsonAnnotation.optString("text"),
													jsonAnnotation.optString("type", "rect"),
													step
											)
									);
								}
								catch (JSONException e) {
									// Tant pis, on passe l'annotation
								}
							}
						}
						annotations.append(Integer.parseInt(pageStr), pageAnnotations);
					}
				}
			}
		}
		return annotations;
	}
}
