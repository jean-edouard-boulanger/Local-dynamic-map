Local dynamic map
=================

Projet universitaire visant à simuler une local dynamic map. Plusieurs véhicules évoluent sur une carte et s'échangent des messages sur les conditions de circulation afin de déterminer un itinéraire optimal, dans le but de diminuer les temps de trajet.

Les librairies suivantes doivent êtres ajoutées à l'environnement de développement afin de pouvoir lancer l'application:
- Jade Framework (Plateforme multi-agent): http://jade.tilab.com
- Jackson (Serialisation JSON): http://jackson.codehaus.org
- Grph (Librairie graphes): http://www.i3s.unice.fr/~hogie/grph/
- JavaFX (User interface): http://www.oracle.com/technetwork/java/javafx/overview/index.html

L'application peut être démmarée en lançant d'abbord le container principal (com.ldm.sma.container MasterContainer), puis en lançant plusieurs véhicules (CarContainer).
