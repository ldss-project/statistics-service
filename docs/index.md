---
title: Homepage
layout: default
nav_order: 1
---

# Statistics Service
{: .no_toc}

## Contenuti
{: .no_toc}

- TOC
{:toc}

---

## Descrizione

Lo **Statistics Service** è un servizio che gestisce i punteggi degli utenti
all'interno di un'applicazione.

Il servizio espone un contratto di tipo _REST_, disponibile al seguente 
[link](/swagger-apis/statistics-service/latest/rest).

## Implementazione

L'implementazione dello **Statistics Service** è descritta dal seguente diagramma delle classi
UML.

![Statistics Service Class Diagram](/statistics-service/resources/images/statistics-service.png)

Come si può vedere dal diagramma, l'implementazione del servizio dipende dal framework
[HexArc](https://github.com/ldss-project/hexarc).

In particolare, il servizio definisce due componenti principali:
- `StatisticsPort`: definisce le funzionalità del servizio.
- `StatisticsHttpAdapter`: espone alcune delle funzionalità della `StatisticsPort` attraverso un
  contratto di tipo _REST_.

Le funzionalità definite dalla `StatisticsPort` sono le seguenti:
- `getScore`: restituisce l'ultimo punteggio di un utente del servizio;
- `getScoreHistory`: restituisce tutte le statistiche di un utente del servizio;
- `getLeaderboard`: restituisce la classifica globale degli utenti del servizio;
- `addScore`: aggiunge un nuovo punteggio per un utente del servizio;
- `deleteScores`: rimuove tutte le statistiche di un utente del servizio.

Tali funzionalità sono definite nei termini dei concetti del dominio del servizio.
In particolare, i modelli relativi a tali concetti sono i seguenti:
- `Score`: modella un punteggio nel servizio;
- `UserScore`: modella il punteggio di un utente nel servizio;
- `UserScoreHistory`: modella le statistiche di un utente nel servizio.

L'implementazione della `StatisticsPort` è modellata dallo `StatisticsModel`.
Lo `StatisticsModel` gestisce la persistenza dei dati nel servizio attraverso una
`PersistentCollection` e per comunicare con la `PersistentCollection` utilizza il
linguaggio delle query `MongoDBQueryLanguage`. Quindi, implementa tutte le funzionalità
della `StatisticsPort` attraverso delle opportune query.

Lo `StatisticsHttpAdapter` e lo `StatisticsModel` possono generare delle eccezioni,
modellate dalla classe `StatisticsServiceException`. In particolare, l'utente che
utilizza il servizio potrebbe essere notificato delle seguenti
`StatisticsServiceException`s:
- `MalformedInputException`: indica all'utente che l'input specificato per una certa
  funzionalità da lui richiesta non è corretto;
- `UserNotFoundException`: indica all'utente che un utente da lui richiesto non è
  stato trovato all'interno del sistema.

## Verifica

Per verificare il sistema, è stata creata una suite di test manuali su
[Postman](https://www.postman.com/), in modo da accertarsi che tutte le funzionalità
esposte dal contratto _REST_ del servizio producessero i risultati attesi.

In futuro, si dovrà creare degli _unit test_ equivalenti, ma automatici. Per fare ciò,
sarà necessario approfondire come creare un database [MongoDB](https://www.mongodb.com)
di tipo _in-memory_ in [Scala](https://scala-lang.org/).