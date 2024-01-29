Il progetto consiste nell'implementazione di un servizio chiamato HOTELIER, che implementa alcune funzionalità di un sito di recensioni di alberghi. 
//
Gli utenti possono cercare hotel in una specifica città, visualizzare informazioni dettagliate sugli hotel e inserire recensioni assegnando punteggi sintetici e specifici alle categorie di Posizione, Pulizia, Servizio e Prezzo.
HOTELIER gestisce solo hotel nelle città capoluogo delle 20 regioni italiane.
Le recensioni sono limitate a punteggi numerici senza supporto per recensioni testuali. 
Il ranking locale degli hotel si basa su parametri come la qualità, la quantità e l'attualità delle recensioni. 
Gli utenti accumulano distintivi in base al numero di recensioni effettuate.

HOTELIER è implementato come un sistema client-server, dove il client gestisce l'interazione con l'utente attraverso un'interfaccia grafica creata usando JavaFX, mentre il server carica le informazioni sugli hotel da un file JSON, gestisce la registrazione e il login degli utenti, e gestisce le recensioni e il ranking degli hotel. Il server invia notifiche agli utenti loggati in caso di cambiamenti nel ranking e persiste periodicamente i dati.
