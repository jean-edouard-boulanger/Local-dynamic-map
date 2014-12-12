
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


public class Fact extends Agent {
	
	private int total = 1;
	private int n = 1;
	
	public class MyBehaviour extends Behaviour{

		@Override
		public void action() {
			ACLMessage message = receive();
			if(message != null){
				String content = message.getContent();
				if(content.indexOf("!")>0){
					content = content.substring(0, content.indexOf("!"));
					n= Integer.parseInt(content)-1;
				}	
				System.out.println(content);
				if(Integer.parseInt(content)> 0){
					setTotal(Integer.parseInt(content));
					content = total+"*"+n;
					n--;
					sendMessageToFact(content);
				}
			}
		}

		private void sendMessageToFact(String content) {
			ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
			message.addReceiver(new AID("multplier@tdia04",AID.ISGUID));
			message.setContent(content);
			myAgent.send(message);
			
		}

		@Override
		public boolean done() {
			if(n<1){
				System.out.println("résultat: " +total);
				return true;
			}
			return false;
		}
		
	}
	
	
	protected void setup(){
		System.out.println(getLocalName());
		addBehaviour(new MyBehaviour());
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}
}
