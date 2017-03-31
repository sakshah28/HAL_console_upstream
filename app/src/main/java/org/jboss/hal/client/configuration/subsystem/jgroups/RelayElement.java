/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.client.configuration.subsystem.jgroups;

import java.util.List;

import elemental.dom.Element;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.IsElement;
import org.jboss.hal.ballroom.Attachable;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Options;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.core.mbui.table.NamedNodeTable;
import org.jboss.hal.core.mvp.HasPresenter;
import org.jboss.hal.dmr.model.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.MetadataRegistry;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.AddressTemplates.SELECTED_RELAY_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.jgroups.StackElement.REMOTE_SITE_ID;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
public class RelayElement implements IsElement, Attachable, HasPresenter<JGroupsPresenter> {

    private final NamedNodeTable<NamedNode> table;
    private final Form<NamedNode> form;
    private JGroupsPresenter presenter;
    private Element section;

    @SuppressWarnings({"ConstantConditions", "HardCodedStringLiteral"})
    RelayElement(final MetadataRegistry metadataRegistry, final Resources resources) {

        Metadata metadata = metadataRegistry.lookup(RELAY_TEMPLATE);

        Options<NamedNode> options = new ModelNodeTable.Builder<NamedNode>(metadata)
                .add((event, api) -> presenter.addRelay())
                .remove((event, api) -> presenter.removeResource(SELECTED_RELAY_TEMPLATE, api.selectedRow().getName(),
                        Names.RELAY))
                .column(NAME, (cell, t, row, meta) -> row.getName())
                .column("Remote Sites", row -> {
                    presenter.showRemoteSites(row);
                    presenter.showStackInnerPage(REMOTE_SITE_ID);
                })
                .build();
        table = new NamedNodeTable<>(Ids.build(Ids.JGROUPS_RELAY, Ids.TABLE_SUFFIX), options);
        form = new ModelNodeForm.Builder<NamedNode>(Ids.build(Ids.JGROUPS_RELAY, Ids.FORM_SUFFIX), metadata)
                .onSave((form, changedValues) -> presenter.saveSingleton(SELECTED_RELAY_TEMPLATE, changedValues,
                        resources.messages().modifySingleResourceSuccess(Names.RELAY)))
                .prepareReset(form -> presenter.resetSingleton(SELECTED_RELAY_TEMPLATE, Names.RELAY, form, metadata))
                .build();

        // @formatter:off
        section = new Elements.Builder()
            .section()
                .h(1).textContent(Names.RELAY).end()
                .p().textContent(metadata.getDescription().getDescription() + ". " + resources.constants().jgroupsRelayAlias()).end()
                .add(table)
                .add(form)
            .end()
        .build();
        // @formatter:on
    }

    @Override
    public Element asElement() {
        return section;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void attach() {
        table.attach();
        form.attach();
        table.bindForm(form);
    }

    @Override
    public void detach() {
        form.detach();
        table.detach();
    }

    @Override
    public void setPresenter(final JGroupsPresenter presenter) {
        this.presenter = presenter;
    }

    void update(List<NamedNode> models) {
        table.update(models);
        boolean enableAddRelay = models.isEmpty();
        table.api().button(0).enable(enableAddRelay);
        form.clear();
    }

}
