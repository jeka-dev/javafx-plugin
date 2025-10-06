package _dev;

import dev.jeka.core.api.tooling.git.JkGit;
import dev.jeka.core.tool.JkDep;
import dev.jeka.core.tool.JkJekaVersionRanges;
import dev.jeka.core.tool.JkPostInit;
import dev.jeka.core.tool.KBean;
import dev.jeka.core.tool.builtins.base.BaseKBean;

class Custom extends KBean {

    @JkPostInit
    private void postInit(BaseKBean baseKBean) {
        JkJekaVersionRanges.setCompatibilityRange(baseKBean.getManifest(),
                "0.11.54",
                "https://raw.githubusercontent.com/jeka-dev/javafx-plugin/master/breaking_versions.txt");
        baseKBean.setVersionSupplier(JkGit.of()::getJkVersionFromTag);
    }

}