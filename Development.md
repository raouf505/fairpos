# Introduction #

Here you will find a quick start and a list of development tools we are using.

# Quick start #

  1. install PostgreSQL V9.0 (_postgresql-9.0.1-1-windows.exe_)
  1. create an empty DB "openbravopos" with default settings (must be a fresh, empty DB; if DB exists, e.g. delete and create a new one)
  1. restore a DB dump shared from a project member via dropbox
  1. remember Postgres user and password!
  1. install OpenBravoPOS 2.30.2 (openbravopos-2.30-windows-installer.exe)
  1. patch this instalation with recent jar, postgresql-9.0-801.jdbc4.jar, reports and locales from your SVN/IDE or shared from a project member via dropbox
  1. modify OpenBravoPOS settings to connect to Postgres DB like this (according to your created DB name and user):

![https://fairpos.googlecode.com/svn/wiki/images/db-settings.gif](https://fairpos.googlecode.com/svn/wiki/images/db-settings.gif)

Deutsche Version:

![https://fairpos.googlecode.com/svn/wiki/images/db-settings_de.gif](https://fairpos.googlecode.com/svn/wiki/images/db-settings_de.gif)


# Installation Jenkins #
  1. java -jar jenkins.war
  1. open in browser: http://localhost:8080/
  1. Jenkins verwalten
  1. Als Windows-Dienst installieren
  1. Confirm with "Yes"
  1. todo: copy plugins, etc.

# Development Process #
  1. The ETL process is developed in Talend (project: [FAIRTRADE](https://fairpos.googlecode.com/svn/trunk/FAIRTRADE/))
  1. the ETL process is based on a customer dependent initial DB (manually edited in OpenBravoPOS as needed for the customer)
  1. The Java code is developed based on OpenBravoPOS in NetBeans IDE (project: [OpenBravoPOS](https://fairpos.googlecode.com/svn/trunk/OpenBravoPOS/))
  1. OpenBravoPOS installation is patched with the modified parts and connected to a DB filled with the ETL process

# ETL nightly Process #
  1. Jenkins runs a nightly job for each supplier
  1. This job compares and, if needed, downloads the supplier file (csv, later also images) with a user and pass over HTTP or FTP via e.g. BeyondCompare
  1. if the compare tool detected changes, the ETL job of the corresponding supplier is started with the parameters
    * input file, image dir, db url, db user and db pass

# Details #

Source based on
  * [Openbravo POS](http://www.openbravo.com/product/pos/) 2.30.2 - [download here](http://sourceforge.net/projects/openbravopos/files/Openbravo%20POS/)

Database connection details
  * using posgresql-9.0-801.jdbc4.jar
  * org.postgresql.Driver
  * jdbc:postgresql://localhost/yourdbname
  * user and pass

Used Tool versions:
  * [PostgreSQL 9.0](http://www.postgresql.org/download/windows) - using pgAdmin III for administration
  * Talend Open Studio (4.1.1.[r50363](https://code.google.com/p/fairpos/source/detail?r=50363)) - for Extract Transform Load of data