# SYP_PROJEKT_23-24_Maier
SYP_PROJEKT_23/24_GRP

Goal of this project is to remote controll the **M-BOT**. For achiving this goal we used to different paths. The first is the Webserver which contains all kind of data and can remote controll the mbot. Used **Adonis js** for the webserver then adding the functionality we need in order for us to get data from the database and the mbot.

<mark> currently working on https://www.mongodb.com/compatibility/using-typescript-with-mongodb-tutorial </mark>


## RUN WEBSERVER

```bash
    cd SYP_Webserver
    node ace serve --watch
```
## Routes of the Webserver are
> - **/** - For **mBot** datatransfer
> - **/mBot** - Webiew of data
> - **/Database** - For requests to the database




The second method we will use is **.NET Maui**. Coded by Dave