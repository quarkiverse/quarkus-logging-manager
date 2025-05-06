import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import '@vaadin/vertical-layout';
import '@vaadin/icon';
import '@vaadin/icons';
import { LitElement, html, css} from 'lit';
import { JsonRpc } from 'jsonrpc';

export class QwcLoggingManagerLoggers extends LitElement {
    jsonRpc = new JsonRpc(this);
    static styles = css`
        .datatable {
            height: 100%;
        }
        .ml-10 {
            margin-left: 10px;
        }
    `

    static properties = {
        _loggers: { state: true },
        _filteredLoggers: { state: true }
    }

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this._refreshLoggers();
    }

    disconnectedCallback() {
        super.disconnectedCallback();
    }

    _refreshLoggers() {
        this.jsonRpc.getLoggers().then(jsonRpcResponse => {
            this._resetLoggers(jsonRpcResponse.result);
        });
    }

    _resetLoggers(result) {
       this._loggers = result;
       this._filteredLoggers = result;
    }

    render() {
        return html`<vaadin-vertical-layout theme="spacing" style="height: 100%">
            <vaadin-text-field
                    placeholder="Search loggers..."
                    style="width: 50%;"
                    class="ml-10"
                    @value-changed="${(e) => {
                        const searchTerm = (e.detail.value || '').trim();
                        const matchesTerm = (value) =>
                                value.toLowerCase().includes(searchTerm.toLowerCase());

                        if (this._loggers) {
                            this._filteredLoggers = this._loggers.filter(
                                    ({ name }) =>
                                            !searchTerm ||
                                            matchesTerm(name)
                            );
                        }
                    }}"
            >
                <vaadin-icon slot="prefix" icon="vaadin:search"></vaadin-icon>
            </vaadin-text-field>
            <vaadin-grid .items="${this._filteredLoggers}" class="datatable ml-10">
                    <vaadin-grid-sort-column auto-width
                        header="Logger"
                        path="name"
                        resizable>
                    </vaadin-grid-sort-column>

                    <vaadin-grid-sort-column auto-width
                        header="Effective Level"
                        path="effectiveLevel"
                        resizable>
                    </vaadin-grid-sort-column>

                    <vaadin-grid-sort-column auto-width
                        header="Configured Level"
                        path="configuredLevel"
                        resizable>
                    </vaadin-grid-sort-column>
                </vaadin-grid>
        </vaadin-vertical-layout>`
    }
}
customElements.define('qwc-logging-manager-loggers', QwcLoggingManagerLoggers);