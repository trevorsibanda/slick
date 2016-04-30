:tocdepth: 2

.. index:: faq

Frequently Asked Questions
##########################

This page contains a list of frequently asked questions and other gotchas to help you get started with Slick. 
Be sure to read the :doc:`Getting Started <gettingstarted>` page for more info. 


General
-------

* What is Slick ?

Slick ("Scala Language-Integrated Connection Kit") is `Typesafe <http://www.typesafe.com>`_'s
Functional Relational Mapping (FRM) library for Scala that makes it easy to work with relational
databases. It allows you to work with stored data almost as if you were using Scala collections
while at the same time giving you full control over when database access happens and which data
is transferred. You can also use SQL directly. Execution of database actions is done
asynchronously, making Slick a perfect fit for your reactive applications based on Play_ and Akka_.

* How do I get Slick ?


Contributing
------------



Other
------------

* How do I generate documentation locally

You can generate this documentation locally by running::

    sbt make-site



