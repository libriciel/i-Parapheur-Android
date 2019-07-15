/*
 * iParapheur Android
 * Copyright (C) 2016-2019 Libriciel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.adullact.iparapheur.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@DatabaseTable(tableName = "Account")
public class Account implements Serializable {

    public static final String DB_FIELD_ID = "Id";
    private static final String DB_FIELD_TITLE = "Title";
    private static final String DB_FIELD_BASE_URL = "BaseUrl";
    private static final String DB_FIELD_LOGIN = "Login";
    private static final @SuppressWarnings("squid:S2068") String DB_FIELD_PASSWORD = "Password";
    private static final String DB_FIELD_USER_FULL_NAME = "UserFullName";
    private static final String DB_FIELD_TENANT = "Tenant";
    private static final String DB_FIELD_API_VERSION = "ApiVersion";
    private static final String DB_FIELD_ACTIVATED = "Activated";
    private static final String DB_FIELD_LAST_REQUEST = "LastRequest";
    private static final String DB_FIELD_DESKS = "Desks";

    @DatabaseField(columnName = DB_FIELD_ID, id = true, index = true)  //
    private String id;

    @DatabaseField(columnName = DB_FIELD_TITLE, canBeNull = false, defaultValue = "")  //
    private String title;

    @DatabaseField(columnName = DB_FIELD_BASE_URL, canBeNull = false, defaultValue = "")  //
    private String serverBaseUrl;

    @DatabaseField(columnName = DB_FIELD_LOGIN, canBeNull = false, defaultValue = "")  //
    private String login;

    @DatabaseField(columnName = DB_FIELD_PASSWORD, canBeNull = false, defaultValue = "")  //
    private String password;

    @DatabaseField(columnName = DB_FIELD_USER_FULL_NAME)  //
    private String userFullName;

    @DatabaseField(columnName = DB_FIELD_TENANT)  //
    private String tenant;

    @DatabaseField(columnName = DB_FIELD_API_VERSION)  //
    private Integer apiVersion;

    @DatabaseField(columnName = DB_FIELD_ACTIVATED)  //
    private boolean activated;

    @DatabaseField(columnName = DB_FIELD_LAST_REQUEST)  //
    private Date lastRequest;

    @ForeignCollectionField(columnName = DB_FIELD_DESKS)  //
    private transient ForeignCollection<Bureau> childrenBureaux;

    private String ticket;


    public Account(String id) {
        this.id = id;
        title = "";
        serverBaseUrl = "";
        login = "";
        password = "";
        tenant = null;
        activated = true;
    }


    public Account(@NonNull String id, @NonNull String title, @NonNull String serverBaseUrl, @NonNull String login, @NonNull String password,
                   @Nullable String tenant, @Nullable Integer apiVersion) {
        this.id = id;
        this.title = title;
        this.serverBaseUrl = serverBaseUrl;
        this.login = login;
        this.password = password;
        this.tenant = tenant;
        this.apiVersion = apiVersion;
        this.activated = true;
    }


    @Override public boolean equals(Object o) {
        if (o instanceof Account) {
            Account toCompare = (Account) o;
            return TextUtils.equals(id, toCompare.id);
        }
        return false;
    }


    @Override public int hashCode() {
        return id.hashCode();
    }

}
