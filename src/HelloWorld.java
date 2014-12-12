import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;


public class HelloWorld extends Agent {
	
	protected void setup(){
		System.out.println(getLocalName()+" HelloWorld");
		addBehaviour(new Behaviour() {
			
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				ACLMessage message = receive();
				if( message !=null && message.getPerformative() == ACLMessage.INFORM){
					System.out.println("Contact "+message.getContent());
				}
				
				
			}
		});
		
	}
}
