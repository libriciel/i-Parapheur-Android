package org.adullact.iparapheur.tab.http;

public enum HttpVerb
{

    OPTIONS, // Idempotent
    GET, // Safe and idempotent
    HEAD, // Safe and idempotent
    POST,
    PUT, // Idempotent
    DELETE, // Idempotent
    TRACE, // Idempotent
    CONNECT

}
