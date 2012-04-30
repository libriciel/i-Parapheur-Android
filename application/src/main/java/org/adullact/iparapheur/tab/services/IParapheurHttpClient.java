package org.adullact.iparapheur.tab.services;

import java.util.List;

import org.adullact.iparapheur.tab.model.Account;
import org.adullact.iparapheur.tab.model.Office;

public interface IParapheurHttpClient
{

    List<Office> getOffices( Account account );

}
