# Android 版 Office 365 API スタート プロジェクト

[![ビルドの状況](https://travis-ci.org/OfficeDev/O365-Android-Start.svg?branch=master)](https://travis-ci.org/OfficeDev/O365-Android-Start)

[日本 (日本語)](/loc/README-ja.md) (日本語)

**目次**

* [変更履歴](#history)
* [デバイスの要件](#requirements)
* [環境を設定する](#setup)
* [Android Studio](#studio) または [Eclipse](#eclipse) を使用してプロジェクトを開く
* [プロジェクトを実行する](#running)
* [コードを理解する](#understanding)
   * [認証](#authentication)
   * [予定表 API](#calendar)
   * [ファイル API](#files)
   * [メール API](#mail)
* [質問とコメント](#questions)
* [その他の技術情報](#resources)

クラウド内にデータを持つ Office 365 のユーザーは多数います。お客様の顧客に OneDrive for Business に保存されたファイルまたは Office 365 Exchange に保存された予定表とメールを操作する新しく革新的な方法をもたらす Android アプリをビルドすることを想像してください。Office 365 SDK スタート プロジェクトは、[Microsoft Open Technologies, Inc (MS Open Tech) 社](http://msopentech.com)の **Office 365 SDK for Android** を使用して、お客様のユーザーの Office 365 および OneDrive for Business のデータをお客様のアプリに統合する方法を示しています。独自のアプリの開始点としてスタート プロジェクトを使用したり、単に使用するパーツを切り取って貼り付けたりすることができます。

スタート プロジェクトは、Office 365 でファイル、予定表、およびメール サービスのエンドポイントを使用する基本操作を示します。また、1 つのアプリで複数の Office 365 サービスを認証する方法も示します。スタート プロジェクトの操作は次のとおりです。

**予定表**

* 既存の予定表のイベントを取得します。
* 新しいイベントを作成します。
* イベントを更新します。
* イベントを削除します。

**OneDrive for Business ファイル**
 
* 既存のファイルとフォルダーを取得します。
* テキスト ファイルを作成します。
* ファイルやフォルダーを削除します。
* テキスト ファイルの内容を読み取ります。
* テキスト ファイルの内容を更新します。
* ファイルをアップロードまたはダウンロードします。

**メール**

* 既存のメール アイテムを取得します。
* メール アイテムを削除します。
* 新しいメールを送信します。

Microsoft では、スタート プロジェクトを更新して、連絡先など他のサービスを追加しています。そのため、必ず後でもう一度確認してください。

<a name="history">
## 変更履歴
2015 年 1 月 23 日:

* メールのサポートが追加されました。
* ファイルのアップロードとダウンロードのサポートが追加されました。

2014 年 12 月 19 日:

* スマート フォンのレイアウトのサポートが追加されました。このサンプルは、縦または横モードでスマート フォンとタブレットの両方で実行するようになりました。
* イベントのページングが予定表クライアントに追加されました。

<a name="requirements">
## デバイスの要件
スタート プロジェクトを実行するには、デバイスが次の要件を満たしている必要があります。

* 画面のサイズが 4 インチ以上である。
* Android の API レベルが 15 以上である。
 
<a name="setup" />
## 環境を設定する

### 前提条件

Android 版 Office 365 API スタート プロジェクトを使用するには、以下が必要です。

* [Android Studio](http://developer.android.com/sdk/index.html) の最新バージョン。
	* オプションで、Eclipse などの [Android Developer Tools](http://developer.android.com/tools/help/adt.html) の最新バージョンを使用できます。
* [Gradle](http://www.gradle.org) ビルド自動化システム バージョン 2.2.1 以上。
* Office 365 アカウント。 [Office 365 開発者プログラムに参加し、Office 365 の 1 年間の無料サブスクリプションを取得](https://profile.microsoft.com/RegSysProfileCenter/wizardnp.aspx?wizid=14b845d0-938c-45af-b061-f798fbb4d170&amp;lcid=1033)しましょう。それには Android アプリの構築を開始し、テストするために必要なすべてのツールとリソースも含まれています。

### Azure でクライアント アプリケーションを作成する

1. [Azure 管理ポータル](https://manage.windowsazure.com/)で、[Active Directory] タブと Office 365 テナントを選択します。
2. **[アプリケーション]** タブを選択し、**[追加]** をクリックします。
3. **[何を行いますか]** 画面で、**[所属組織が開発しているアプリケーションの追加]** をクリックします。
4. アプリケーションの名前を追加します。スタート プロジェクトを使用している場合は、名前を O365-Android-Start にする必要があります。**[ネイティブ クライアント アプリケーション]** をクリックし、[次へ] 矢印をクリックします。
5. リダイレクト URI を追加します。URI は、実際のリソースにリンクする必要はありませんが、有効な SSL の URI である必要があります。たとえば、"https://localhost/starterproject" で試してみてください。アプリケーションの追加を終了するには、チェック マークをクリックします。
6. 作成したアプリケーションを開き、**[構成]** をクリックします。アプリケーションのクライアント ID を書き留めておきます。これは、後でスタート プロジェクトを構成する際に必要になります。

### クライアント アプリケーションにアクセス許可を付与します。

スタート プロジェクトでは、クライアント アプリケーションに Office 365 の特定のアクセス許可のセットがある必要があります。 

1. [Azure 管理ポータル](https://manage.windowsazure.com/)で、[Active Directory] タブと Office 365 テナントを選択します。
2. **[アプリケーション]** タブを選択し、構成するアプリケーションをクリックします。
3. **[他のアプリケーションへのアクセス許可]** セクションで、次のアクセス許可を追加します。
   * [**Office 365 SharePoint Online** アプリケーション (1)] を追加し、[**ユーザー ファイルの読み取りと書き込み** のアクセス許可 (2)] を選択します。
   ![SharePoint アプリケーションのアクセス許可](/readme-images/o365-sharepoint-permissions.JPG)
   * **Office 365 Exchange Online** アプリケーション (3) を追加してから、**ユーザーの予定表の読み取りと書き込み**、**ユーザーとしてメールを送信**、および **ユーザーのメールの読み取りと書き込み** の各アクセス許可 (4) を選択します。
   ![Exchange アプリケーションのアクセス許可](/readme-images/o365-exchange-permissions.JPG)
4. 変更を保存します。

Android スタート プロジェクトの構成の詳細は、現在の開発環境によって異なります。詳細な説明はできませんが、必要な基本手順について説明します。

<a name="studio" />
## Android Studio を使用してプロジェクトを開く

1. developer.android.com の[指示](http://developer.android.com/sdk/installing/adding-packages.html)に従って、[Android Studio](http://developer.android.com/tools/studio/index.html#install-updates) をインストールし、Android SDK パッケージを追加します。
2. [Android 版 Office 365 スタート プロジェクト](https://github.com/OfficeDev/O365-Android-Start)をダウンロードするか、クローンを作成します。
3. Android Studio を起動します。
	1. **[既存の Android Studio プロジェクトを開く]** を選択します。
	2. **O365-Android-Start** フォルダーの **build.gradle** ファイルを選択し、**[OK]** をクリックします。
4. com.microsoft.office365.starter.helpers パッケージの constants.java ファイルを開きます。
	1. CLIENT_ID 定数を検索して、その String 値を Azure Active Directory に登録されているクライアント ID と同じ値に設定します。
	2. REDIRECT_URI 定数を検索して、その String 値を Azure Active Directory に登録されているリダイレクト URI と同じ値に設定します。

> 注:スタート プロジェクトは、Gradle を使用して必要な依存関係を宣言します。依存関係は次のとおりです。
>  
> *  [Android 版 Azure Active Directory 認証ライブラリ](https://github.com/AzureAD/azure-activedirectory-library-for-android)。
> * [Office 365 SDK for Android](https://github.com/OfficeDev/Office-365-SDK-for-Android)。

<a name="eclipse" />
## Eclipse を使用してプロジェクトを開く

1. developer.android.com の[指示](http://developer.android.com/tools/help/adt.html)に従って、Android Developer Tools と Eclipse をインストールおよび構成します。
2. [Android 版 Active Directory Azure ライブラリ](https://github.com/AzureAD/azure-activedirectory-library-for-android)をダウンロードするか、クローンを作成します。
3. Eclipse を起動して、アプリ用に新しいワークスペースを作成します。
4. Active Directory Azure ライブラリから新しいワークスペースに AuthenticationActivity プロジェクトをインポートします。
	1. Android サポート ライブラリを AuthenticationActivity プロジェクトに追加します。これを実行するには、プロジェクトを右クリックし、[Android ツール]、[サポートのライブラリの追加] の順に選択します。
	2.  [gson ライブラリ](https://code.google.com/p/google-gson/)の最新バージョンをダウンロードします。
	3. gson jar ファイルを AuthenticationActivity プロジェクトの libs フォルダーに追加します。
5. [Android 版 Office 365 スタート プロジェクト](https://github.com/OfficeDev/O365-Android-Start)をダウンロードするか、クローンを作成します。
6. コマンド プロンプトを開きます。
	1. スタート プロジェクトが配置されているパスにある *app\src\main* フォルダーに移動します。
	2. `gradle -b eclipse.gradle eclipse` を実行します。
7. スタート プロジェクトの *app\src\main* フォルダーを Eclipse ワークスペースにインポートします。
8. Eclipse で、メイン プロジェクトのプロパティを開きます。[Android] タブに移動します。
	1. *[プロジェクトのビルド ターゲット]* で、*API レベル 15*を選択します。
	2. *[ライブラリ]* セクションで、AuthenticationActivity プロジェクトを追加します。
9. com.microsoft.office365.starter.helpers パッケージの constants.java ファイルを開きます。
	1. CLIENT_ID 定数を検索して、その String 値を Azure Active Directory に登録されているクライアント ID と同じ値に設定します。
	2. REDIRECT_URI 定数を検索して、その String 値を Azure Active Directory に登録されているリダイレクト URI と同じ値に設定します。

<a name="running" />
## プロジェクトを実行する

スタート プロジェクトをビルドしたら、エミュレーターまたはデバイス上で実行できます。

1. プロジェクトを実行します。
3. [サインイン] ボタンをクリックして、資格情報を入力します。
4. [予定表]、[ファイル]、または [メール] ボタンをクリックしてデータの操作を開始します。
  
<a name="understanding" />
## コードを理解する

スタート プロジェクトでは、**O365FileListModel**、**O365CalendarModel**、および **O365MailItemsModel** という 3 つのオブジェクトを使用して、Office 365 での操作を管理します。これらのオブジェクトは、Office 365 SDK for Android にある **SharePointClient** オブジェクトと **OutlookClient** オブジェクトへの呼び出しをラップします。`O365APIsStart_Application.getFileClient()`、`O365APIsStart_Application.getCalendarClient()`、および `O365APIsStart_Application.getMailClient()` の各メソッドで、SDK のオブジェクトがどのように作成されているかを確認してください。

<a name="authentication" />
### 認証

Office 365 SDK for Android では、認証に Android 版 Azure Active Directory Library (ADAL) を使用します。ADAL は、OAuth2 のプロトコルのサポート、Web API とユーザー レベルの承認との統合、および 2 要素認証を実行します。

**AuthenticationController** オブジェクトは、ADAL からトークンを取得して、アプリケーションに戻すことを管理します。

<a name="calendar" />
### 予定表 API

**O365CalendarModel** オブジェクトは、Office 365 Exchange の予定表にある予定イベントの作成、更新、および削除を実行する API の操作をラップします。 

**getEventList(int pageSize,int skipToEventNumber)** メソッドは、Office 365 の予定表からイベントのリストを取得し、イベントのページをローカルのリストに読み込みます。リストに対する変更、削除、および追加の内容は、**postUpdatedEvent**、**postDeletedEvent**、および **postCreatedEvent** の各メソッドによって Office 365 予定表に非同期的に送信されます。 

<a name="files" />
### ファイル API

**O365FileListModel** オブジェクトは、OneDrive for Business に格納されているファイルの作成、更新、および削除を実行する API 操作をラップします。

**getFilesAndFoldersFromService** メソッドは、OneDrive for Business に格納されているすべてのファイルおよびフォルダーのリストを取得し、そのリストをローカルの配列に読み込みます。ローカルのファイル リストに対する変更、削除、および追加の内容は、**postUpdatedFileContents**、**postUploadFileToServer**、**postDeleteSelectedFileFromServer**、および **postNewFileToServer** の各メソッドによって非同期的に OneDrive for Business に送信されます。 

**getFileContentsFromServer** メソッドは、選択されたファイルの内容を含む **O365FileModel** オブジェクトを返します。

<a name="mail" />
### メール API

**O365MailItemsModel** オブジェクトは、Office 365 Exchange のメールボックスにあるメール アイテムの作成、更新、および削除を実行する API の操作をラップします。 

**getMessageList(int pageSize,int skipToMessageNumber)** メソッドは、Office 365 メールボックスからメール アイテムのリストを取得して、アイテムのページをローカル リストに読み込みます。このリストの削除内容と、メールボックスから送信されたメールは、**postDeleteMailItem** および **postNewMailToServer** の各メソッドによって Office 365 の予定表に非同期的に送信されます。 

<a name="questions" />
## 質問とコメント

O365 Android Starter プロジェクトについて、Microsoft にフィードバックをお寄せください。質問や提案につきましては、このリポジトリの「[問題](https://github.com/OfficeDev/O365-Android-Start/issues)」セクションに送信できます。

Office 365 開発全般の質問につきましては、「[スタック オーバーフロー](http://stackoverflow.com/questions/tagged/Office365+API)」に投稿してください。質問またはコメントには、必ず [Office365] および [API] のタグを付けてください。
  
<a name="resources" />
## その他の技術情報

* [Office 365 API プラットフォームの概要](http://msdn.microsoft.com/office/office365/howto/platform-development-overview)
* [ファイルの REST 操作のリファレンス](https://msdn.microsoft.com/office/office365/api/files-rest-operations)
* [予定表の REST 操作のリファレンス](http://msdn.microsoft.com/office/office365/api/calendar-rest-operations)
* [メールの REST 操作のリファレンス](https://msdn.microsoft.com/office/office365/api/mail-rest-operations)
* [Microsoft Office 365 API ツール](https://visualstudiogallery.msdn.microsoft.com/a15b85e6-69a7-4fdf-adda-a38066bb5155)
* [Office デベロッパー センター](http://dev.office.com/)
* [Office 365 API スタート プロジェクトおよびサンプル コード](http://msdn.microsoft.com/office/office365/howto/starter-projects-and-code-samples)
* [Android 用 Office 365 Connect のサンプル](https://github.com/OfficeDev/O365-Android-Connect)
* [Android 用 Office 365 コード スニペット](https://github.com/OfficeDev/O365-Android-Snippets)
* [Android 用 Office 365 プロファイル サンプル](https://github.com/OfficeDev/O365-Android-Profile)


## 著作権
Copyright (c) Microsoft. All rights reserved.

