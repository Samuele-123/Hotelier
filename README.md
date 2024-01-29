#Hotelier

##Il progetto consiste nell'implementazione di un servizio chiamato HOTELIER, che implementa alcune funzionalità di un sito di recensioni di alberghi. 

##Specifiche:
Gli utenti possono cercare hotel in una specifica città, visualizzare informazioni dettagliate sugli hotel e inserire recensioni assegnando punteggi sintetici e specifici alle categorie di Posizione, Pulizia, Servizio e Prezzo.
HOTELIER gestisce solo hotel nelle città capoluogo delle 20 regioni italiane.
Le recensioni sono limitate a punteggi numerici senza supporto per recensioni testuali. 
Il ranking locale degli hotel si basa su parametri come la qualità, la quantità e l'attualità delle recensioni. 
Gli utenti accumulano distintivi in base al numero di recensioni effettuate.

HOTELIER è implementato come un sistema client-server, dove il client gestisce l'interazione con l'utente attraverso un'interfaccia grafica creata usando JavaFX, mentre il server carica le informazioni sugli hotel da un file JSON, gestisce la registrazione e il login degli utenti, e gestisce le recensioni e il ranking degli hotel. Il server invia notifiche agli utenti loggati in caso di cambiamenti nel ranking e persiste periodicamente i dati.

##Istruzioni per la compilazione

Sono pubbliche le cartelle di Client e Server, all’interno di ognuna di queste cartelle sono presenti altre due sotto cartelle: una contenente il jar eseguibile
(con le librerie per l’interfaccia grafica e i file necessari) e l’altra con i file sorgente (nella sottocartella lib del client non è fornita nuovamente la libreria javafx-sdk-21.0.1, in quanto già presente nella cartella dell’eseguibile per non aumentare ulteriormente la grandezza complessiva).
Si noti che a causa dell’esportazione del file JAR e corrispondente modifica dei path, i file config(del server) nella cartella degli esegubili e nella cartella dei sorgenti sono diversi.

###SERVER
Per eseguire il server, basta eseguire il comando:
java -jar server.jar

###CLIENT
Per eseguire il jar risulta necessaria una JDK >17 e l’esecuzione tramite il comando:
java --module-path ./javafx-sdk-21.0.1/lib --add-modules javafx.controls,javafx.fxml -jar client.jar

Se invece si ha una SDK con javaFX incluso:
java -jar client.jar

In allegato è fornita la javafx-sdk per windows, è comunque possibile scaricare la versione
corretta per il sistema che si sta usando al seguente link:
https://gluonhq.com/products/javafx/
