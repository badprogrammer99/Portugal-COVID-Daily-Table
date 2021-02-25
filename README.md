Programs that make tracking and analyzing the Portuguese COVID data a easier task. The description of each one of them is written below.

# Portugal COVID Daily Table CLI

A Java command-line program that prints out a table about the latest Portugal COVID-19 stats. The stats that are shown in this table are:

* 👥 Confirmed cases
* ✔️Recovered cases
* ☠️Deaths
* 🏥 Hospitalized cases
* 🛌 Intensive care unit cases
* 😷 Active cases
* 📈 The 7-day rolling average of the stats above (except active cases)
* 📊 Variation of new confirmed cases in relation to the same day in the previous weeks
* 🌎 The contribution of each geographic area to the new cases nationwide.

It also prints out information about some regional stats (confirmed cases and deaths).

**TODO**

* Refactor the code some more. Too much repetition still going on.
* Add the capability of automatically detecting COVID-19 reports as soon as they are added to the DGS website.
* Create clients that can show the table printed out by this program.
* Add push notifications to the previously mentioned clients by taking advantage of the functionality in the second point.
* Write unit tests.

Here is how a table printed out by this program can look like with some dope CSS applied:
https://codepen.io/badprogrammer99/pen/NWbaNXo

# Portugal COVID Daily Table Server

An extension of the original Portugal COVID Daily Table command-line program, programmed using the Spring Boot framework.

Available endpoints:

* /tables/html: Retrieves the COVID Daily Table in an HTML format.
* /tables/reddit: Retrieves the COVID Daily Table in a Reddit markdown format.

To get notifications about new data being available, subscribe to the following Pusher channel, using the information provided below:

* Channel name: energized-atoll-189
* Channel event: covid-data-available
* Channel cluster: EU

# Portugal COVID Daily Table Android

An Android application that simply shows the HTML table data in a WebView. It also notifies the user when COVID data comes out.
