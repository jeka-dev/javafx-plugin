package _dev;

import dev.jeka.core.tool.JkDep;
import dev.jeka.core.tool.JkJekaVersionRanges;
import dev.jeka.core.tool.JkPostInit;
import dev.jeka.core.tool.KBean;
import dev.jeka.core.tool.builtins.base.BaseKBean;

@JkDep("org.junit.jupiter:junit-jupiter:6.0.3")
class Custom extends KBean {

    @JkPostInit
    private void postInit(BaseKBean baseKBean) {
        JkJekaVersionRanges.setCompatibilityRange(baseKBean.getManifest(),
                "0.11.59",
                "https://raw.githubusercontent.com/jeka-dev/javafx-plugin/master/breaking_versions.txt");
    }

}