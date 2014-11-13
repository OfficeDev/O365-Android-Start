# Office 365 APIs Starter Project for Android

**Table of contents**

* [Setting up your environment](#setting_up_your_environment)
* [Running the starter project](#running_the_starter_project)
* [Understanding the code](#understanding_the_code)
   * [Authentication](#authentication)
   * [Calendar API](#calendar_api)
   * [Files API](#files_api)
* [Questions and comments](#questions_and_comments)
* [Additional resources](#additional_resources)

There are millions of Office 365 users with data in the cloud; imagine building an Android app that gives your customers new and innovative ways to work with their files stored on OneDrive or their calendars stored in Office 365 Exchange. The Office 365 SDK starter project shows you how to use the **Office 365 SDK for Android** from [Microsoft Open Technologies, Inc (MS Open Tech)](http://msopentech.com) to integrate your users' Office 365 and OneDrive data into your app. You can use the starter project as a starting point for your own app; or you can just cut and paste the parts that you want to use.

The starter project shows you basic operations using the Files and Calendar service endpoints on Office 365. It also demonstrates how to authenticate with multiple Office 365 services in a single app. The starter project operations include:

**Calendar**

* Get existing calendar events.
* Create new events.
* Update events.
* Delete events.

**OneDrive Files**
 
* Get existing files and folders.
* Create text files.
* Delete files or folders.
* Read text file contents.
* Update text file contents.

We'll be updating the starter project to add other services, such as Email, so make sure to check back.

## Setting up your environment

To use the Office 365 APIs starter project for Android you need the following:

* The latest version of the [Android Developer Tools](http://developer.android.com/sdk/index.html) including Eclipse.
* An Office 365 account. You can sign up for [an Office 365 Developer subscription](http://msdn.microsoft.com/en-us/library/office/fp179924.aspx) that includes all of the tools and resources that you need to start building and testing Android apps.
* The [Active Directory Azure Library for Android](https://github.com/AzureAD/azure-activedirectory-library-for-android). You'll need to either clone the repository or  [download the zip file](https://github.com/AzureAD/azure-activedirectory-library-for-android/archive/master.zip).
* The [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android). You'll need to either clone the repository or [download the zip file](https://github.com/OfficeDev/Office-365-SDK-for-Android/archive/master.zip).

The details of configuring the Android starter project depend on your current development environment. While we can't give you detailed instructions, we can give you the basic steps that you need to follow.

1. Install and configure the Android Developer Tools and Eclipse according to the [instructions](http://developer.android.com/sdk/installing/index.html) on developer.android.com.
2. [Sign up](http://msdn.microsoft.com/en-us/library/office/fp179924.aspx) for a developer account on Office 365.
3. Create a new client application in Azure.
   * In the [Azure management portal](https://manage.windowsazure.com/), select the Active Directory tab and an Office 365 tenant.
   * Choose the **Applications** tab and click **Add**.
   * In the **What do you want to do** screen click **Add an application my organization is developing**.
   * Add the name of your application. If you're using the starter project, the name should be *O365-Android-Start*. Click **NATIVE CLIENT APPLICATION** and then click the next arrow.
   * Add a redirect URI. The URI does not need to link to an actual resource, but it does need to be a valid SSL URI. Try "https://localhost/starterproject", for example. Click the check mark to finish adding the application.
   * Open the application that you just created and click **Configure**. Make a note of the application's client ID, you'll need that later to configure the starter project.
<br>
<br>

4. Grant permissions to your client application.
   * In the [Azure management portal](https://manage.windowsazure.com/),
   select the Active Directory tab and an Office 365 tenant.
   * Choose the **Applications** tab and click the application that you want to configure.
   * In the **permissions to other applications** section, add the following two permissions.
	   * Add the **Office 365 SharePoint Online** application (1), and select the **Edit or delete users' files** permission (2).

	   ![SharePoint application permissions](/readme-images/o365-sharepoint-permissions.JPG)
	   * Add the **Office 365 Exchange Online** application (3), and select the **Have full access to users' calendars** permission (4).

	   ![Exchange application permissions](/readme-images/o365-exchange-permissions.JPG)
   * Save the changes.

5. Download or clone the
   [Active Directory Azure Library for Android](https://github.com/AzureAD/azure-activedirectory-library-for-android).
6. Start Eclipse and create a new workspace for your app.
7. Import the AuthenticationActivity project from the Active Directory Azure Library into your new workspace.
8. Add the Android support library to the AuthenticationActivity project. To do this, right-click the project, choose *Android Tools*, and then *Add Support Library*.
9.  Download the latest version of the [gson library](https://code.google.com/p/google-gson/).
10. Add the gson jar file to the libs folder of the AuthenticationActivity project.
11. Download or clone the
   [Office 365 Starter Project for Android](https://github.com/officedev/o365-android-start/).
12. Import the starter project into your Eclipse workspace.
13. Download the latest [guava library](http://code.google.com/p/guava-libraries/).
14. Add the guava jar file to the libs folder of the o365-android-start project.
15. Add the jar files from the Office 365 SDK for Android. Either download the jar files from Bintray, or clone and build the Office 365 SDK for Android, and then copy the jar files to your project.

	**To download the jar files:**
   Download the jar files for the [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android) from [Bintray](https://bintray.com/msopentech/Maven/Office-365-SDK-for-Android/view) You need to add the following jar files to the libs folder of the starter app so that it compiles and runs correctly:
    * odata-engine-android-impl-0.9.3.jar
    * outlook-services-0.9.3.jar
    * file-services-0.9.3.jar
    * discovery-services-0.9.3.jar
	<br>Note: You can use version 0.9.3 or later of the jars.

	**To build the jar files:**
	
	1. Clone the [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android).
	2. Go to the sdk directory.
	3. Run `.\gradlew clean`.
	4. Run `.\gradlew assemble`.

16. Open the constants.java file in the com.microsoft.office365.starter package.
17. Find the CLIENT_ID constant and set its String value equal to the client id you registered in Azure Active Directory.
18. Find the REDIRECT_URI constant and set its String value equal to the redirect URI you registered in Azure Active Directory.


## Running the starter project

Once you've built the starter project you can run it on an emulator or device. Note that at this time it only runs for landscape mode on tablets.

1. Run the project.
3. Click the "Sign in" button and enter your credentials.
4. Click the calendar or file button to start working with your data.
  
## Understanding the code

The starter project uses two objects, **O365FileListModel** and **O365CalendarModel** to manage interactions with Office 365. These two objects wrap calls to the **SharePointClient** and **OutlookClient** objects in the Office 365 SDK for Android. Look at the  `O365APIsStart_Application.getFileClient()` and `O365APIsStart_Application.getCalendarClient()` methods to see how the SDK 
objects are created.

### Authentication

The Office 365 SDK for Android uses the Azure Active Directory Library (ADAL) for Android for authentication. The ADAL provides protocol support for OAuth2, Web API integration with user level consent, and two-factor authentication.

The **Authentication** object  manages getting a token from ADAL and returning it to your application.

### Calendar API

The **O365CalendarModel** object wraps the API operations that create, update and delete calendar events in an Office 365 Exchange calendar. 

The **getEventList()** method gets a list of events from the Office 365 calendar and loads the events into a local list. Changes, deletions, and additions to this list are posted asynchronously to the Office 365 calendar by the **postUpdatedEvent**, **postDeletedEvent**, and **postCreatedEvent** methods. 

### Files API

The **O365FileListModel** object wraps the API operations that create, update, and delete files stored on OneDrive.

The **getFilesAndFoldersFromService** method gets a list of all of the files and folders that are stored on OneDrive and loads the list into a local array. Changes, deletions, and additions to the local list of files are posted asynchronously to OneDrive by the **postUpdatedFileContents**, **postDeleteSectectedFileFromList**, and **postNewFileToServer** methods. 

The **getFileDetailsFromService** method returns an **O365FileModel** object containing the selected files contents.

## Questions and comments

We'd love to get your feedback on this Android starter kit. You can send your questions and suggestions to us:

* In the [Issues](https://github.com/OfficeDev/O365-Android-Start/issues) section of this repository.
* On [Stack Overflow](http://stackoverflow.com/questions/tagged/Office365+API). 
  Make sure that your questions or comments are tagged with [Office365] and [API].
  
## Additional resources

* [Office 365 APIs documentation](http://msdn.microsoft.com/office/office365/howto/platform-development-overview)
* [File REST operations reference](http://msdn.microsoft.com/en-us/office/office365/api/files-rest-operations)
* [Calendar REST operations reference](http://msdn.microsoft.com/en-us/office/office365/api/calendar-rest-operations)
* [Microsoft Office 365 API Tools](https://visualstudiogallery.msdn.microsoft.com/7e947621-ef93-4de7-93d3-d796c43ba34f)
* [Office Dev Center](http://dev.office.com/)
* [Office 365 APIs Starter Project for Windows](https://github.com/OfficeDev/Office-365-APIs-Starter-Project-for-Windows)
* [Office 365 APIs Starter Project for ASP.NET MVC](https://github.com/OfficeDev/Office-365-APIs-Starter-Project-for-ASPNETMVC)


## Copyright
Copyright (c) Microsoft. All rights reserved.
