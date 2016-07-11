# Office 365 APIs Starter Project for Android

[![Build Status](https://travis-ci.org/OfficeDev/O365-Android-Start.svg?branch=master)](https://travis-ci.org/OfficeDev/O365-Android-Start)

[日本 (日本語)](/loc/README-ja.md) (Japanese)

**Table of contents**

* [Change History](#history)
* [Device requirements](#requirements)
* [Set up your environment](#setup)
* Open the project using [Android Studio](#studio) or [Eclipse](#eclipse)
* [Run the project](#running)
* [Understanding the code](#understanding)
   * [Authentication](#authentication)
   * [Calendar API](#calendar)
   * [Files API](#files)
   * [Mail API](#mail)
* [Questions and comments](#questions)
* [Additional resources](#resources)

There are millions of Office 365 users with data in the cloud; imagine building an Android app that gives your customers new and innovative ways to work with their files stored on OneDrive for Business or their calendars and mail stored in Office 365 Exchange. The Office 365 SDK starter project shows you how to use the **Office 365 SDK for Android** from [Microsoft Open Technologies, Inc (MS Open Tech)](http://msopentech.com) to integrate your users' Office 365 and OneDrive for Business data into your app. You can use the starter project as a starting point for your own app; or you can just cut and paste the parts that you want to use.

The starter project shows you basic operations using the Files, Calendar and Mail service endpoints on Office 365. It also demonstrates how to authenticate with multiple Office 365 services in a single app. The starter project operations include:

**Calendar**

* Get existing calendar events.
* Create new events.
* Update events.
* Delete events.

**OneDrive for Business Files**
 
* Get existing files and folders.
* Create text files.
* Delete files or folders.
* Read text file contents.
* Update text file contents.
* Upload or download files.

**Mail**

* Get existing mail items.
* Delete mail item.
* Send new mail.

We'll be updating the starter project to add other services, such as Contacts, so make sure to check back.

<a name="history">
## Change History
January 23, 2015:

* Added Mail support.
* Added File upload and download support.

December 19, 2014:

* Added smartphone layout support. The sample now runs on both tablets and smartphones in landscape or portrait mode.
* Added paging of events to Calendar client.

<a name="requirements">
## Device requirements
To run the starter project your device needs to meet the following requirements:

* A screen size of 4 inches or larger.
* Android API level 15 or later.
 
<a name="setup"/>
## Set up your environment

### Prerequisites

To use the Office 365 APIs starter project for Android you need the following:

* The latest version of [Android Studio](http://developer.android.com/sdk/index.html).
	* Optionally, you can use the latest version of the [Android Developer Tools](http://developer.android.com/tools/help/adt.html) including Eclipse.
* The [Gradle](http://www.gradle.org) build automation system version 2.2.1 or later.
* An Office 365 account. You can [Join the Office 365 Developer Program and get a free 1 year subscription to Office 365](https://profile.microsoft.com/RegSysProfileCenter/wizardnp.aspx?wizid=14b845d0-938c-45af-b061-f798fbb4d170&lcid=1033) that includes all of the tools and resources that you need to start building and testing Android apps.

### Create a client application in Azure

1. In the [Azure management portal](https://manage.windowsazure.com/), select the Active Directory tab and an Office 365 tenant.
2. Choose the **Applications** tab and click **Add**.
3. In the **What do you want to do** screen click **Add an application my organization is developing**.
4. Add the name of your application. If you're using the starter project, the name should be *O365-Android-Start*. Click **NATIVE CLIENT APPLICATION** and then click the next arrow.
5. Add a redirect URI. The URI does not need to link to an actual resource, but it does need to be a valid SSL URI. Try "https://localhost/starterproject", for example. Click the check mark to finish adding the application.
6. Open the application that you just created and click **Configure**. Make a note of the application's client ID, you'll need that later to configure the starter project.

### Grant permissions to your client application.

The starter project requires that your client application has a specific set of permissions on Office 365. 

1. In the [Azure management portal](https://manage.windowsazure.com/),
select the Active Directory tab and an Office 365 tenant.
2. Choose the **Applications** tab and click the application that you want to configure.
3. In the **permissions to other applications** section, add the following permission.
   * Add the **Office 365 SharePoint Online** application (1), and select the **Read and write user files** permission (2).
   ![SharePoint application permissions](/readme-images/o365-sharepoint-permissions.JPG)
   * Add the **Office 365 Exchange Online** application (3), and select the **Read and write user calendars**, **Send mail as a user**, and **Read and write user mail** permissions (4).
   ![Exchange application permissions](/readme-images/o365-exchange-permissions.JPG)
4. Save the changes.

The details of configuring the Android starter project depend on your current development environment. While we can't give you detailed instructions, we can give you the basic steps that you need to follow.

<a name="studio"/>
## Open the project using Android Studio

1. Install [Android Studio](http://developer.android.com/tools/studio/index.html#install-updates) and add the Android SDK packages according to the [instructions](http://developer.android.com/sdk/installing/adding-packages.html) on developer.android.com.
2. Download or clone the
   [Office 365 Starter Project for Android](https://github.com/OfficeDev/O365-Android-Start).
3. Start Android Studio.
	1. Choose **Open an existing Android Studio project**.
	2. Select the **build.gradle** file in the **O365-Android-Start** folder and click **OK**.
4. Open the constants.java file in the com.microsoft.office365.starter.helpers package.
	1. Find the CLIENT_ID constant and set its String value equal to the client id you registered in Azure Active Directory.
	2. Find the REDIRECT_URI constant and set its String value equal to the redirect URI you registered in Azure Active Directory.

> Note: The starter project declares the required dependencies using Gradle. The dependencies are:
> 
> *  The [Azure Active Directory Authentication Library for Android](https://github.com/AzureAD/azure-activedirectory-library-for-android).
> * The [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android).

<a name="eclipse"/>
## Open the project using Eclipse

1. Install and configure the Android Developer Tools and Eclipse according to the [instructions](http://developer.android.com/tools/help/adt.html) on developer.android.com.
2. Download or clone the
   [Active Directory Azure Library for Android](https://github.com/AzureAD/azure-activedirectory-library-for-android).
3. Start Eclipse and create a new workspace for your app.
4. Import the AuthenticationActivity project from the Active Directory Azure Library into your new workspace.
	1. Add the Android support library to the AuthenticationActivity project. To do this, right-click the project, choose *Android Tools*, and then *Add Support Library*.
	2.  Download the latest version of the [gson library](https://code.google.com/p/google-gson/).
	3. Add the gson jar file to the libs folder of the AuthenticationActivity project.
5. Download or clone the
   [Office 365 Starter Project for Android](https://github.com/OfficeDev/O365-Android-Start).
6. Open a command prompt.
	1. Go to the *app\src\main* folder in the path where the starter project is located.
	2. Run `gradle -b eclipse.gradle eclipse`
7. Import the *app\src\main* folder of the starter project into your Eclipse workspace.
8. In Eclipse, open the properties of the main project. Go to the Android tab.
	1. In *Project Build Target* choose *API level 15*.
	2. In the *Library* section, add the AuthenticationActivity project.
9. Open the constants.java file in the com.microsoft.office365.starter.helpers package.
	1. Find the CLIENT_ID constant and set its String value equal to the client id you registered in Azure Active Directory.
	2. Find the REDIRECT_URI constant and set its String value equal to the redirect URI you registered in Azure Active Directory.

<a name="running"/>
## Run the project

Once you've built the starter project you can run it on an emulator or device.

1. Run the project.
3. Click the "Sign in" button and enter your credentials.
4. Click the calendar, file, or mail button to start working with your data.
  
<a name="understanding"/>
## Understanding the code

The starter project uses three objects, **O365FileListModel**, **O365CalendarModel**, and **O365MailItemsModel** to manage interactions with Office 365. These objects wrap calls to the **SharePointClient** and **OutlookClient** objects in the Office 365 SDK for Android. Look at the  `O365APIsStart_Application.getFileClient()`, `O365APIsStart_Application.getCalendarClient()`, and `O365APIsStart_Application.getMailClient()` methods to see how the SDK objects are created.

<a name="authentication"/>
### Authentication

The Office 365 SDK for Android uses the Azure Active Directory Library (ADAL) for Android for authentication. The ADAL provides protocol support for OAuth2, Web API integration with user level consent, and two-factor authentication.

The **AuthenticationController** object  manages getting a token from ADAL and returning it to your application.

<a name="calendar"/>
### Calendar API

The **O365CalendarModel** object wraps the API operations that create, update and delete calendar events in an Office 365 Exchange calendar. 

The **getEventList(int pageSize,int skipToEventNumber)** method gets a list of events from the Office 365 calendar and loads pages of events into a local list. Changes, deletions, and additions to this list are posted asynchronously to the Office 365 calendar by the **postUpdatedEvent**, **postDeletedEvent**, and **postCreatedEvent** methods. 

<a name="files"/>
### Files API

The **O365FileListModel** object wraps the API operations that create, update, and delete files stored on OneDrive for Business.

The **getFilesAndFoldersFromService** method gets a list of all of the files and folders that are stored on OneDrive for Business and loads the list into a local array. Changes, deletions, and additions to the local list of files are posted asynchronously to OneDrive for Business by the **postUpdatedFileContents**,  **postUploadFileToServer**, **postDeleteSelectedFileFromServer**, and **postNewFileToServer** methods. 

The **getFileContentsFromServer** method returns an **O365FileModel** object containing the selected files contents.

<a name="mail"/>
### Mail API

The **O365MailItemsModel** object wraps the API operations that create, update and delete mail items in an Office 365 Exchange mailbox. 

The **getMessageList(int pageSize,int skipToMessageNumber)** method gets a list of mail items from the Office 365 mailbox and loads pages of items into a local list. Deletions to this list, and mail sent from the mailbox are posted asynchronously to the Office 365 calendar by the **postDeleteMailItem**, and **postNewMailToServer** methods. 

<a name="questions"/>
## Questions and comments

We'd love to get your feedback on the O365 Android Starter project. You can send your questions and suggestions to us in the [Issues](https://github.com/OfficeDev/O365-Android-Start/issues) section of this repository.

Questions about Office 365 development in general should be posted to [Stack Overflow](http://stackoverflow.com/questions/tagged/Office365+API). Make sure that your questions or comments are tagged with [Office365] and [API].
  
<a name="resources"/>
## Additional resources

* [Office 365 APIs platform overview](http://msdn.microsoft.com/office/office365/howto/platform-development-overview)
* [File REST operations reference](https://msdn.microsoft.com/office/office365/api/files-rest-operations)
* [Calendar REST operations reference](http://msdn.microsoft.com/office/office365/api/calendar-rest-operations)
* [Mail REST operations reference](https://msdn.microsoft.com/office/office365/api/mail-rest-operations)
* [Microsoft Office 365 API Tools](https://visualstudiogallery.msdn.microsoft.com/a15b85e6-69a7-4fdf-adda-a38066bb5155)
* [Office Dev Center](http://dev.office.com/)
* [Office 365 APIs starter projects and code samples](http://msdn.microsoft.com/office/office365/howto/starter-projects-and-code-samples)
* [Office 365 Connect Sample for Android](https://github.com/OfficeDev/O365-Android-Connect)
* [Office 365 Code Snippets for Android](https://github.com/OfficeDev/O365-Android-Snippets)
* [Office 365 Profile sample for Android](https://github.com/OfficeDev/O365-Android-Profile)


## Copyright
Copyright (c) Microsoft. All rights reserved.

