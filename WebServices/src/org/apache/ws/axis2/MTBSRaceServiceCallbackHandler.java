
/**
 * MTBSRaceServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package org.apache.ws.axis2;

    /**
     *  MTBSRaceServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class MTBSRaceServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public MTBSRaceServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public MTBSRaceServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for connectDB method
            * override this method for handling normal response from connectDB operation
            */
           public void receiveResultconnectDB(
                    org.apache.ws.axis2.ConnectDBResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from connectDB operation
           */
            public void receiveErrorconnectDB(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for warteBisStart method
            * override this method for handling normal response from warteBisStart operation
            */
           public void receiveResultwarteBisStart(
                    org.apache.ws.axis2.WarteBisStartResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from warteBisStart operation
           */
            public void receiveErrorwarteBisStart(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRennenID method
            * override this method for handling normal response from getRennenID operation
            */
           public void receiveResultgetRennenID(
                    org.apache.ws.axis2.GetRennenIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRennenID operation
           */
            public void receiveErrorgetRennenID(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for actPosListe method
            * override this method for handling normal response from actPosListe operation
            */
           public void receiveResultactPosListe(
                    org.apache.ws.axis2.ActPosListeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from actPosListe operation
           */
            public void receiveErroractPosListe(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getInfoURL method
            * override this method for handling normal response from getInfoURL operation
            */
           public void receiveResultgetInfoURL(
                    org.apache.ws.axis2.GetInfoURLResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getInfoURL operation
           */
            public void receiveErrorgetInfoURL(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for rennenListe method
            * override this method for handling normal response from rennenListe operation
            */
           public void receiveResultrennenListe(
                    org.apache.ws.axis2.RennenListeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from rennenListe operation
           */
            public void receiveErrorrennenListe(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for insertTeilnahme method
            * override this method for handling normal response from insertTeilnahme operation
            */
           public void receiveResultinsertTeilnahme(
                    org.apache.ws.axis2.InsertTeilnahmeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from insertTeilnahme operation
           */
            public void receiveErrorinsertTeilnahme(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getKonfiguration method
            * override this method for handling normal response from getKonfiguration operation
            */
           public void receiveResultgetKonfiguration(
                    org.apache.ws.axis2.GetKonfigurationResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getKonfiguration operation
           */
            public void receiveErrorgetKonfiguration(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteTeilnahme method
            * override this method for handling normal response from deleteTeilnahme operation
            */
           public void receiveResultdeleteTeilnahme(
                    org.apache.ws.axis2.DeleteTeilnahmeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteTeilnahme operation
           */
            public void receiveErrordeleteTeilnahme(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTeilnahmeID method
            * override this method for handling normal response from getTeilnahmeID operation
            */
           public void receiveResultgetTeilnahmeID(
                    org.apache.ws.axis2.GetTeilnahmeIDResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTeilnahmeID operation
           */
            public void receiveErrorgetTeilnahmeID(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for insertUser method
            * override this method for handling normal response from insertUser operation
            */
           public void receiveResultinsertUser(
                    org.apache.ws.axis2.InsertUserResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from insertUser operation
           */
            public void receiveErrorinsertUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for insertActPos1 method
            * override this method for handling normal response from insertActPos1 operation
            */
           public void receiveResultinsertActPos1(
                    org.apache.ws.axis2.InsertActPos1Response result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from insertActPos1 operation
           */
            public void receiveErrorinsertActPos1(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getRegCode method
            * override this method for handling normal response from getRegCode operation
            */
           public void receiveResultgetRegCode(
                    org.apache.ws.axis2.GetRegCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getRegCode operation
           */
            public void receiveErrorgetRegCode(java.lang.Exception e) {
            }
                


    }
    