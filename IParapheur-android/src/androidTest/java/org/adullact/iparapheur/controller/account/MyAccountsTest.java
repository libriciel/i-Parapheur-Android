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
package org.adullact.iparapheur.controller.account;

import android.support.annotation.NonNull;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.adullact.iparapheur.model.Account;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class MyAccountsTest {

	// <editor-fold desc="Static utils">

	private static @NonNull Account createAccount(@NonNull String number) {

		Account account = MyAccounts.INSTANCE.addAccount();
		account.setServerBaseUrl("baseurl" + number + ".org");
		account.setTitle("title_" + number);
		account.setLogin("login_" + number);
		account.setPassword("password_@\"!()\\_" + number);

		return account;
	}

	private static void cleanupData() {

		List<Account> accountList = new ArrayList<>(MyAccounts.INSTANCE.getAccounts());
		for (Account account : accountList)
			MyAccounts.INSTANCE.delete(account);
	}

	// </editor-fold desc="Static utils">

	@Test public void testSave() {

		cleanupData();

		createAccount("01");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts().size(), 1);

		createAccount("02");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts().size(), 2);

		createAccount("03");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts().size(), 3);

		cleanupData();
	}

	@Test public void testDelete() {

		// Create

		cleanupData();

		createAccount("01");
		createAccount("02");
		createAccount("03");

		// Delete and test

		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts().size(), 3);

		List<Account> accountList = new ArrayList<>(MyAccounts.INSTANCE.getAccounts());
		for (Account account : accountList)
			MyAccounts.INSTANCE.delete(account);

		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts().size(), 0);
	}

	@Test public void testGetAccount() {

		// Create

		cleanupData();

		Account inputAccount01 = createAccount("01");
		Account inputAccount02 = createAccount("02");

		// Fetch and test

		MyAccounts.INSTANCE.getAccounts();

		Account savedAccount01 = MyAccounts.INSTANCE.getAccount(inputAccount01.getId());
		Assert.assertNotNull(savedAccount01);
		Assert.assertEquals(savedAccount01.getId(), inputAccount01.getId());
		Assert.assertEquals(savedAccount01.getServerBaseUrl(), inputAccount01.getServerBaseUrl());
		Assert.assertEquals(savedAccount01.getTitle(), inputAccount01.getTitle());
		Assert.assertEquals(savedAccount01.getLogin(), inputAccount01.getLogin());
		Assert.assertEquals(savedAccount01.getPassword(), inputAccount01.getPassword());

		Account savedAccount02 = MyAccounts.INSTANCE.getAccount(inputAccount02.getId());
		Assert.assertNotNull(savedAccount02);
		Assert.assertEquals(savedAccount02.getId(), inputAccount02.getId());
		Assert.assertEquals(savedAccount02.getServerBaseUrl(), inputAccount02.getServerBaseUrl());
		Assert.assertEquals(savedAccount02.getTitle(), inputAccount02.getTitle());
		Assert.assertEquals(savedAccount02.getLogin(), inputAccount02.getLogin());
		Assert.assertEquals(savedAccount02.getPassword(), inputAccount02.getPassword());

		cleanupData();
	}
}

