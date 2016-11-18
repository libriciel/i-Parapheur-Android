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
package org.adullact.iparapheur.utils;

import junit.framework.Assert;

import org.junit.Test;


/**
 * @see org.adullact.iparapheur.utils.StringUtils
 */
public class StringUtilsTest {

	@Test public void fixIssuerDnX500NameStringOrder() throws Exception {

		String input = "OU=ADULLACT-Projet,E=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,O=ADULLACT-Projet,ST=Herault,C=FR";
		String value = StringUtils.fixIssuerDnX500NameStringOrder(input);
		String expected = "EMAIL=systeme@adullact.org,CN=AC ADULLACT Projet\\, g2,OU=ADULLACT-Projet,O=ADULLACT-Projet,ST=Herault,C=FR";

		Assert.assertEquals(value, expected);
	}
}