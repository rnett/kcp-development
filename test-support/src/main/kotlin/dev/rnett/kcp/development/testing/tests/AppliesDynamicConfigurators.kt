package dev.rnett.kcp.development.testing.tests

import dev.rnett.kcp.development.testing.configuration.TestConfigurator
import dev.rnett.kcp.development.testing.configuration.applyDynamicConfigurators

/**
 * A marker interface guaranteeing that any [TestConfigurator] annotations on implementations of this class will be applied (e.g. by [applyDynamicConfigurators]).
 */
interface AppliesDynamicConfigurators {
}