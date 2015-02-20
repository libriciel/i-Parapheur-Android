package org.adullact.iparapheur.controller.rest.api;

import org.adullact.iparapheur.controller.rest.RESTUtils;
import org.adullact.iparapheur.model.Bureau;
import org.adullact.iparapheur.utils.IParapheurException;

import java.util.ArrayList;

/**
 * Created by jmaire on 09/06/2014.
 * API i-Parapheur version 2
 * <p/>
 * de la v4.1.00 / v3.5.00 (comprise)
 * a la v4.2.00 / v3.6.00 (exclue)
 * <p/>
 * Quasiment la meme l'API 1, excepte la recuperation
 * des bureaux. On passe du POST au GET.
 */
public class RESTClientAPI2 extends RESTClientAPI1 {

	@Override
	public ArrayList<Bureau> getBureaux() throws IParapheurException {
		String url = buildUrl(ACTION_GET_BUREAUX);
		//String body = "{\"username\": \"" + MyAccounts.INSTANCE.getSelectedAccount().getLogin() + "\"}";
		//Log.d( IParapheurHttpClient.class, "REQUEST on " + FOLDERS_PATH + ": " + requestBody );
		//return ModelMapper.getBureaux(RESTUtils.post(url, body));
		return modelMapper.getBureaux(RESTUtils.get(url));
	}

}
