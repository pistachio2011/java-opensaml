/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.profile.action;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.opensaml.profile.ProfileException;
import org.opensaml.profile.context.ProfileRequestContext;

import net.shibboleth.utilities.java.support.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentValidationException;
import net.shibboleth.utilities.java.support.component.ValidatableComponent;


//TODO perf metrics

/**
 * Base class for profile actions.
 * 
 * @param <InboundMessageType> type of in-bound message
 * @param <OutboundMessageType> type of out-bound message
 */
public abstract class AbstractProfileAction<InboundMessageType, OutboundMessageType> extends
        AbstractIdentifiableInitializableComponent implements ValidatableComponent,
        ProfileAction<InboundMessageType, OutboundMessageType> {

    /**
     * Constructor.
     * 
     * Initializes the ID of this action to the class name.
     */
    public AbstractProfileAction() {
        super();

        setId(getClass().getName());
    }

    /** {@inheritDoc} */
    public synchronized void setId(String componentId) {
        super.setId(componentId);
    }
    
    /** {@inheritDoc} */
    public void validate() throws ComponentValidationException {
        
    }

    /** {@inheritDoc} */
    public void execute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext)
            throws ProfileException {
        
        // The try/catch logic is designed to favor a checked ProfileException raised by
        // the doExecute step over any unchecked errors. It also favors an unchecked error
        // from doExecute over one raised by doPostExecute.
        
        if (doPreExecute(profileRequestContext)) {
            try {
                doExecute(profileRequestContext);
            } catch (ProfileException e) {
                try {
                    doPostExecute(profileRequestContext, e);
                } catch (RuntimeException re) {
                    Logger.getInstance(AbstractProfileAction.class).warn(
                            "Runtime exception thrown by doPostExecute of " + getId(), re);
                }
                throw e;
            } catch (RuntimeException e) {
                try {
                    doPostExecute(profileRequestContext, e);
                } catch (RuntimeException re) {
                    Logger.getInstance(AbstractProfileAction.class).warn(
                            "Runtime exception thrown by doPostExecute of " + getId(), re);
                }
                throw e;
            }

            doPostExecute(profileRequestContext);
        }
    }
    
    /**
     * Called prior to execution, actions may override this method to perform pre-processing for a
     * request.
     * 
     * <p>If false is returned, execution will not proceed, and the action should attach an
     * {@link org.opensaml.profile.context.EventContext} to the context tree to signal how
     * to continue with overall workflow processing.</p>
     * 
     * <p>If returning successfully, the last step should be to return the result of the
     * superclass version of this method.</p>
     * 
     * @param profileRequestContext the current IdP profile request context
     * @return  true iff execution should proceed
     * 
     * @throws ProfileException thrown if there is a problem executing the profile action
     */
    protected boolean doPreExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext)
            throws ProfileException {
        return true;
    }    
    
    /**
     * Performs this action. Actions must override this method to perform their work.
     * 
     * @param profileRequestContext the current IdP profile request context
     * 
     * @throws ProfileException thrown if there is a problem executing the profile action
     */
    protected void doExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext)
            throws ProfileException {
        throw new ProfileException("This operation is not implemented.");
    }

    /**
     * Called after execution, actions may override this method to perform post-processing for a
     * request.
     * 
     * <p>Actions must not "fail" during this step and will not have the opportunity to signal
     * events at this stage. This method will not be called if {@link #doPreExecute} fails, but
     * is called if an exception is raised by {@link #doExecute}.</p>
     * 
     * @param profileRequestContext the current IdP profile request context
     */
    protected void doPostExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext) {
    }    

    /**
     * Called after execution, actions may override this method to perform post-processing for a
     * request.
     * 
     * <p>Actions must not "fail" during this step and will not have the opportunity to signal
     * events at this stage. This method will not be called if {@link #doPreExecute} fails, but
     * is called if an exception is raised by {@link #doExecute}.</p>
     * 
     * <p>This version of the method will be called if an exception is raised during execution of
     * the action. The overall action result will be to raise this error, so any errors inadvertently
     * raised by this method will be logged and superseded.</p>
     * 
     * <p>The default implementation simply calls the error-less version of this method.</p>
     * 
     * @param profileRequestContext the current IdP profile request context
     * @param e an exception raised by the {@link #doExecute} method
     */
    protected void doPostExecute(
            @Nonnull final ProfileRequestContext<InboundMessageType, OutboundMessageType> profileRequestContext,
            @Nonnull final Exception e) {
        doPostExecute(profileRequestContext);
    }    
}