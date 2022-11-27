# apptesting
This is a sample of some software that I wrote for an internship at a company that created web apps.
My mentor told me their app testing process was becoming time-consuming and was a month-long process. I took the initiative
to learn the Selenium and Apache POI APIs myself and create this software which automated their testing process.
This piece of software is able to run tests in about 30 minutes and print the results to an Excel sheet.

I have redacted all proprietary information from this code sample. The example provided here is a very narrow use case
to test whether the web app redirected properly, but it can be easily customized for other success conditions.
The App class should be customized to the particular app being tested. Each of the question classes are a subclass of the
abstract Question class, and contain methods to test the questions under different conditions.
