/*
 *  Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
 */

package com.microsoft.office365.starter.helpers;

import org.json.JSONException;
import org.json.JSONObject;

public class APIErrorMessageHelper
{
    // Takes the string returned from Outlook service in the
    // onFailure event, parses for the JSON object, and gets
    // the actual error message
    public static String getErrorMessage(String result)
    {
        String errorMessage = "";
        try {

            // Gets the JSON object out of the result string
            String responseJSON = result
                    .substring(result.indexOf("{"), result.length());

            JSONObject jObject = new JSONObject(responseJSON);
            JSONObject error = (JSONObject) jObject.get("error");
            errorMessage = error.getString("message");

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            errorMessage = e.getMessage();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            errorMessage = ex.getMessage();
        }
        return errorMessage;
    }

}
// *********************************************************
//
// O365-Android-Start, https://github.com/OfficeDev/O365-Android-Start
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// *********************************************************
