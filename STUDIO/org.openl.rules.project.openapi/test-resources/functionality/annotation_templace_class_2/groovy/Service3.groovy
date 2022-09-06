package org.openl.rules.project.openapi.test;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.openl.rules.context.IRulesRuntimeContext;
import org.openl.rules.ruleservice.core.interceptors.RulesType;

@Path(value = "prefix")
interface Service3 {
    @POST
    @Path("/BankRatingCalculation")
    Object BankRatingCalculation(IRulesRuntimeContext runtimeContext, @RulesType("Bank") Object object);
}

