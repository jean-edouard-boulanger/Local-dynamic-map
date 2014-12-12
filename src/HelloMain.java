import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class HelloMain {
	public static void main(String[] args) {
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl("127.0.0.1",1099,null,false);
		ContainerController cc = rt.createAgentContainer(p);
		try {
			AgentController fact = cc.createNewAgent("factoriel","Fact",null);
			fact.start();
			AgentController mult = cc.createNewAgent("multplier","Mult",null);
			mult.start();
			
		}catch(Exception ex) {
		}	
	}
	
}
