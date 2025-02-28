package org.openl.spring.env;

import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

/**
 * An adapter to access internal instantiation of {@link FirewallPropertyResolver}.
 *
 * @author Yury Molchan
 */
class FirewallEnvironment extends StandardEnvironment {

    FirewallEnvironment(MutablePropertySources propertySources) {
        super(propertySources);
    }

    @Override
    protected ConfigurablePropertyResolver createPropertyResolver(MutablePropertySources propertySources) {
        return new FirewallPropertyResolver(propertySources);
    }

    FirewallPropertyResolver getRawPropertyResolver() {
        return (FirewallPropertyResolver) getPropertyResolver();
    }

    @Override
    protected void customizePropertySources(MutablePropertySources propertySources) {
        // Prevent default customization. Let's configure it in PropertySourcesLoader in the centralized manner.
    }
}
