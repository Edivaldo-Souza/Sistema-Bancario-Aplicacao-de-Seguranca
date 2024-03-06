package kdc;

import cript.ImplAES;
import cript.ImplHmac;

public class Distribuidor {
	
	public static final String HASHKEY = "chave893842";
	public static ImplHmac hash = new ImplHmac();
	public static ImplAES aes = new ImplAES();
	
}
