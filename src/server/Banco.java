package server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cript.ImplHmac;
import kdc.Distribuidor;
import mode.Conta;

public class Banco {
	
	static List<Conta> contas = new ArrayList<Conta>();
	static Conta contaAtual;
	
	public static String receberDados(String encodedMsg, String hash) {
		
		String reply;
		String decryptedMsg = Distribuidor.aes.decrypt(encodedMsg);
		
			String newHash;
			try {
				newHash = ImplHmac.Hmac(Distribuidor.HASHKEY, decryptedMsg);
				if(newHash.equals(hash)) {
					reply = operacoes(decryptedMsg);
					return enviarDados(reply);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	private static String enviarDados(String dados) {
		try {
			String hash = ImplHmac.Hmac(Distribuidor.HASHKEY, dados);
			String encryptedMsg = Distribuidor.aes.encrypt(dados);
			
			return (encryptedMsg+"_"+hash);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "Não foi possível estabelecer conexão";
	}

	private static String operacoes(String requisicao) {
		if(requisicao.equals("sair")) {
			for(int i = 0; i<contas.size(); i++) {
				if(contaAtual.getAccoutNumber().equals(contas.get(i).getAccoutNumber())) {
					contas.set(i, contaAtual);
				}
			}
			
			return "Sessao Encerrada";
		}
		if(requisicao.toCharArray()[0]=='1') {
			String[] dados = requisicao.split("_");
			
			for(Conta c : contas) {
				if(c.getAccoutNumber().equals(dados[1]) && c.getPassword().equals(dados[2])) {
					contaAtual = c;
					return "auth";
				}
			}
			return "Falha no Login";
		}
		if(requisicao.toCharArray()[0]=='2') {
			String[] dados = requisicao.split("_");
			String accountNumber = "";
			
			Random r = new Random();
			for(int i = 0; i<5; i++) {
				accountNumber += Integer.toString(r.nextInt(10));
			}
			Conta novaConta = new Conta(accountNumber,dados[5],dados[1],dados[2],dados[3],dados[4]);
			
			contas.add(novaConta);
			return contas.get(contas.size()-1).toString();
			
		}
		else if(requisicao.toCharArray()[0]=='d') {
			String[] dados = requisicao.split("_");
			Double deposito = Double.parseDouble(dados[1]);
			
			contaAtual.setSaldo(contaAtual.getSaldo()+deposito);
			return "Deposito realizado! Saldo Atual: R$"+contaAtual.getSaldo();
		}
		else if(requisicao.toCharArray()[0]=='s') {
			String[] dados = requisicao.split("_");
			Double saque = Double.parseDouble(dados[1]);
			
			if(contaAtual.getSaldo()>saque) {
				contaAtual.setSaldo(contaAtual.getSaldo()-saque);
			}
			else
				return "Quantia Indisponivel para saque!";
			
			return "Saque realizado! Saldo Atual: R$"+contaAtual.getSaldo();
		}
		else if(requisicao.toCharArray()[0]=='c') {
			return "Saldo Atual: R$"+contaAtual.getSaldo();
		}
		else if(requisicao.toCharArray()[0]=='t') {
			String[] dados = requisicao.split("_");
			Double transferencia = Double.parseDouble(dados[2]);
			for(int i = 0; i<contas.size(); i++) {
				if(contas.get(i).getAccoutNumber().equals(dados[1])) {
					if(contaAtual.getSaldo()>transferencia) {
						contaAtual.setSaldo(contaAtual.getSaldo()-transferencia);
						contas.get(i).setSaldo(contas.get(i).getSaldo()+transferencia);
						
						return "Transferencia de R$"+contas.get(i).getSaldo()+" para a conta "+dados[1];
					}
					return "Quantia Indisponivel para transferencia!";
				}
			}
			return "Conta nao encontrada";
			
		}
		else if(requisicao.toCharArray()[0]=='i') {
			String[] dados = requisicao.split("_");
			double inv = Double.parseDouble(dados[2]);
			double taxa;
			List<Double> projs = new ArrayList<Double>();  
			String projecao;
			DecimalFormat format = new DecimalFormat("#.00");
			
			if(dados[1].equals("1")) {
				taxa = 0.005;
			}else taxa =0.015;
			
			
			for(int i = 1; i<13; i++) {
				inv += inv*taxa;
				if(i%3==0 && i!=9) {
					double temp = inv;
					projs.add(temp);
				}
			}
			
			projecao = "Projecao para:\n"
					+ "3 meses: R$"+format.format(projs.get(0))+"\n"
					+ "6 meses: R$"+format.format(projs.get(1))+"\n"
					+ "12 meses: R$"+format.format(projs.get(2));
			
			return projecao;
		}
		
		return "Operação nao reconhecida";
	}
}

