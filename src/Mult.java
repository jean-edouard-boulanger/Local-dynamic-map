import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


public class Mult extends Agent {

	public class MyBehaviour extends Behaviour{

		@Override
		public void action() {
			ACLMessage message = receive();
			if(message != null){
				String par = message.getContent();
				ACLMessage reply = message.createReply();
				if (par.contains("*")) {
					String[] parameters = par.split("\\*");
					int res = Integer.parseInt(parameters[0].trim())* Integer.parseInt(parameters[1].trim());
					System.out.println("muliplication " + Integer.parseInt(parameters[0].trim()) +  " * " + Integer.parseInt(parameters[1].trim()) + " = " + res);
					reply.setPerformative(ACLMessage.INFORM);
					reply.setContent(String.valueOf(res));
				}
				else {
					System.out.println("error with operation");
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("unknown operator");

				}
				myAgent.send(reply);
			}
			
		}

		@Override
		public boolean done() {
			return false;
		}
		
	}
	
	protected void setup(){
		System.out.println(getLocalName());
		addBehaviour(new MyBehaviour());
	}
		
}
