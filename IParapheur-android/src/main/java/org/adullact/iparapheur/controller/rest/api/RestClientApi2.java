/*
 * <p>iParapheur Android<br/>
 * Copyright (C) 2016 Adullact-Projet.</p>
 *
 * <p>This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.</p>
 *
 * <p>This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.</p>
 *
 * <p>You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.</p>
 */
package org.adullact.iparapheur.controller.rest.api;

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
public class RestClientApi2 extends RestClientApi1 {

	@Override public ArrayList<Bureau> getBureaux() throws IParapheurException {
		return null;
	}

}
