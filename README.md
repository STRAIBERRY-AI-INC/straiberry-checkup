[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="image/Github-1.png" alt="Straiberry">
  </a>

  <h1 align="center">Straiberry Checkup</h1>

  <p align="center">
    Oral hygien checkup with straiberry
    <br />
    <a href="https://www.straiberry.com/">Straiberry</a>
    ·
    <a href="https://github.com/STRAIBERRY-AI-INC/straiberry-checkup/issues">Report Bug</a>
    ·
    <a href="https://github.com/STRAIBERRY-AI-INC/straiberry-checkup/issues">Request Feature</a>
  </p>
</div>


<!-- GETTING STARTED -->
## Getting Started
Using following instruction you can implement our checkup feature in your application. For now this SDK contains our original UI that makes implementing this feature very easy and fast.

### 1-Adding dependecies
1. Add it in your root build.gradle at the end of repositories:
  ```sh
  	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
2. Add the dependency
  ```sh
  	dependencies {
	    implementation 'com.github.STRAIBERRY-AI-INC:straiberry-checkup:1.1.0'
	}
  ```
  
  ### 2- Create an application
  Go to http://sdk.straiberry.com/admin and create a new application. Enter your package name and copy the app_id .
  
  ### 3- Save App id
  Save your app_id in string.xml or save it in other way's as you wish:
  
```sh 
<string name="checkup_sdk_key">b56f95d9-8727-4d29-ae9b-410324784e79</string>
```

### 4- Set SDK info
After getting app id, you can use StraiberryCheckupSdkInfo class to set some requirements that SDK needs them to do a checkup:

 * #### Token Info
    In your main activity set the app id that you get from panel and your package name:
  
    ```sh 
    StraiberryCheckupSdkInfo.setTokenInfo(appId, packageName)
    ```
 * #### Display Name
    In the process of checkup we need a name so we can show the result and user can share it. It is recommended to use the name of your users as display name. You can  set this in the first place that your app gets user info such as home page :
    
    ```sh 
    StraiberryCheckupSdkInfo.setDisplayName(user name) 
    ```
    
 * #### User Avatar
    We also need user avatar to show it in Share Result page. (it’s optional but good for UI):
    ```sh
    StraiberryCheckupSdkInfo.setUserAvatar(avatar url)
    ```
    
 * #### Language
    For now our SDK support Persian(“fa“) and English(“en“). The language will be set based on your app language or device language.
    
### Checkup UI    
If you are using navigation component in your app import our checkup navigation graph into your nav graph xml file and navigate to it with nav controller:
```sh 
<include app:graph="@navigation/navigation_checkup" />
```
or you can just navigate to checkup using nav controller: 
```sh
findNavController().navigate(R.id.navigation_checkup)
```

The process of checkup contains five sections:

1. Checkup : contains main checkup page called FragmentCheckup . In this page user can select a type of checkup.
2. Help : Contains some instruction and helping for doing a checkup using main camera.
3. Questions : Some checkup type’s needs answering questions before going to camera page. this page called FragmentCheckupQuestion 
4. Camera : This page is responsible for detecting jaw’s and taking pictures of them.
5. Result : Shows the result of checkup and problems that have been found.
<br />
<br />
<br />
<br />

_Copyright © `2022`, `StrAIberry`_  
_All rights reserved._

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of the `StrAIberry` nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL `StrAIberry` BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
