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

## Esecuzione

Per eseguire il sistema è disponibile un jar al seguente
[link](https://github.com/ldss-project/statistics-service/releases).

Per eseguire il jar è sufficiente utilizzare il seguente comando:
```shell
java -jar statistics-service-<version>.jar \
--mongodb-connection MONGODB_CONNECTION_STRING
```

In particolare, il jar permette di specificare i seguenti argomenti a linea di comando:
- `--mongodb-connection MONGODB_CONNECTION_STRING`: obbligatorio. Permette di specificare
  la stringa (`MONGODB_CONNECTION_STRING`) per connettersi all'istanza di
  [MongoDB](https://www.mongodb.com) che sarà utilizzata dal servizio per memorizzare i propri
  dati.
- `--mongodb-database DATABASE_NAME`: opzionale. Permette di indicare il nome del database (`DATABASE_NAME`)
  all'interno dell'istanza di [MongoDB](https://www.mongodb.com) specificata in cui il servizio memorizzerà i
  propri dati. Default: `statistics`.
- `--mongodb-collection COLLECTION_NAME`: opzionale. Permette di indicare il nome della collezione
  (`COLLECTION_NAME`) all'interno del database [MongoDB](https://www.mongodb.com) specificato in cui il
  servizio memorizzerà i propri dati. Default: `scores`.
- `--http-host HOST`: opzionale. Permette di indicare il nome dell'host (`HOST`) su cui sarà esposto il
  contratto _REST_ del servizio. Default: `localhost`.
- `--http-port PORT`: opzionale. Permette di indicare la porta dell'host (`PORT`) su cui sarà esposto il
  contratto _REST_ del servizio. Default: `8080`.
- `--allowed-origins ORIGIN_1;ORIGIN_2;...;`: opzionale. Permette di indicare una lista dei siti web che
  saranno autorizzati a comunicare con il servizio. Tale lista consiste in una sequenza di URL separati
  da `;`. Default: _nessun sito web autorizzato_.

In alternativa, un'immagine per eseguire il jar è stata pubblicata anche su [Docker](https://www.docker.com/).
Per eseguire il servizio tramite [Docker](https://www.docker.com/) è sufficiente utilizzare il seguente comando:
```shell
docker run -it jahrim/io.github.jahrim.chess.statistics-service:<version> \
--mongodb-connection MONGODB_CONNECTION_STRING
```