![](EL_struktuuri-_ja_investeerimisfondid_horisontaalne.jpg)

ET(JUHEND.md) | EN

# DHX-adapter usage guide

![](DHX.PNG)  ![](X-ROAD.PNG)

## Introduction

DHX adpater is Java software library that implements [DHX protocol](https://github.com/e-gov/DHX/) functionality for sending documents, receiving documents and generating address book. 

This guide is intended for software developers, who wish to use DHX protocol in their document management system (DMS).

Source code of DHX Adapter is located in the url https://github.com/e-gov/DHX-adapter

It contains three sub-packages
- **dhx-adapter-core** – contains classes for creating and parsing XML objects (Capsule and SOAP), excpetion classes and some general utility classes
- **dhx-adapter-ws** – contains classes for sending document (SOAP client), for generating address book (SOAP client) and for receiving documents (SOAP Service endpoint)
- **dhx-adapter-server** – a separately used adapter server (Variant C), that caches received documents in local database and offers SOAP interface similar to old [DVK interface](https://github.com/e-gov/DVK)


