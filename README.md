# MX-5 Orologio (Android Auto)

App per Android Auto che mostra l'ora sullo schermo dell'auto, navigabile interamente con la
rotella, senza bisogno del touchscreen.

Applicazione indipendente sviluppata da Alberto Bernacchi, non affiliata, sponsorizzata o
approvata da Mazda Motor Corporation.

## Schermate

- **Orologio digitale** (schermata iniziale): ora corrente (ore:minuti:secondi) in grande,
  con giorno della settimana e data in italiano sotto (es. "Lunedì 31 Agosto").
- **Orologio analogico**: quadrante a tutto schermo con lancette ore/minuti bianche e lancetta
  dei secondi rossa.
- **Cronometro**: formato minuti:secondi.millesimi, con pulsanti Avvia/Pausa e Azzera.

Dalla schermata dell'orologio digitale, i due pulsanti in alto passano rispettivamente
all'orologio analogico e al cronometro. Da lì si torna alla schermata digitale con il
pulsante in alto oppure con il tasto indietro della rotella.

## Creare l'APK gratis senza Android Studio (GitHub Actions)

1. Crea un account gratuito su GitHub (se non ne hai già uno).
2. Crea un nuovo repository. Se vuoi evitare il consumo dei minuti Actions del piano gratuito
   privato, rendilo **Public**.
3. Carica tutto il contenuto di questa cartella nel repository (non lo ZIP dentro un'altra
   cartella).
4. Vai in **Actions** → **Build APK** → **Run workflow**.
5. Quando il workflow termina con una spunta verde, apri la relativa esecuzione.
6. In fondo trovi **Artifacts** → `MX5-AutoClock-debug`.
7. Scarica l'archivio dell'artifact e dentro troverai `MX-5 Orologio.apk`.

Non serve installare Android Studio sul PC.

## Installazione sul telefono

Serve KingInstaller (https://github.com/fcaronte/KingInstaller/releases) con Android Auto in
modalità sviluppatore (https://www.smartworld.it/guide/come-abilitare-opzioni-sviluppatore-android-auto.html),
esattamente come per le altre app della stessa serie.

## Licenza

Applicazione per uso personale, non destinata alla distribuzione o vendita a terzi.
© 2026 Alberto Bernacchi. Tutti i diritti riservati. "Mazda" e "MX-5" sono marchi registrati
dei rispettivi proprietari, citati in questo repository e nell'app solo a scopo descrittivo.
