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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.adullact.iparapheur.model.Account;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;


@RunWith(AndroidJUnit4.class)
@MediumTest
public class MyAccountsTest {

	private Context mContext;

	@Before public void setup() {
		mContext = InstrumentationRegistry.getContext();
	}

	// <editor-fold desc="Static utils">

	private static @NonNull Account createAccount(@NonNull Context context, @NonNull String number) {

		Account account = MyAccounts.INSTANCE.addAccount(context);
		account.setServerBaseUrl("baseurl" + number + ".org");
		account.setTitle("title_" + number);
		account.setLogin("login_" + number);
		account.setPassword("password_@\"!()\\_" + number);

		return account;
	}

	private static void cleanupData(@NonNull Context context) {

		List<Account> accountList = new ArrayList<>(MyAccounts.INSTANCE.getAccounts(context));
		for (Account account : accountList)
			MyAccounts.INSTANCE.delete(context, account);
	}

	// </editor-fold desc="Static utils">

	@Test public void testSave() {

		cleanupData(mContext);

		createAccount(mContext, "01");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts(mContext).size(), 1);

		createAccount(mContext, "02");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts(mContext).size(), 2);

		createAccount(mContext, "03");
		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts(mContext).size(), 3);

		cleanupData(mContext);
	}

	@Test public void testDelete() {

		// Create

		cleanupData(mContext);

		createAccount(mContext, "01");
		createAccount(mContext, "02");
		createAccount(mContext, "03");

		// Delete and test

		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts(mContext).size(), 3);

		List<Account> accountList = new ArrayList<>(MyAccounts.INSTANCE.getAccounts(mContext));
		for (Account account : accountList)
			MyAccounts.INSTANCE.delete(mContext, account);

		Assert.assertEquals(MyAccounts.INSTANCE.getAccounts(mContext).size(), 0);
	}

	@Test public void testGetAccount() {

		// Create

		cleanupData(mContext);

		Account inputAccount01 = createAccount(mContext, "01");
		Account inputAccount02 = createAccount(mContext, "02");

		// Fetch and test

		MyAccounts.INSTANCE.getAccounts(mContext);

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

		cleanupData(mContext);
	}
}
