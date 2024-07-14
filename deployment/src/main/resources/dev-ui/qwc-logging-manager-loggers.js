import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import { LitElement, html, css} from 'lit';
import { columnBodyRenderer } from '@vaadin/grid/lit.js';
import { JsonRpc } from 'jsonrpc';

export class QwcLoggingManagerLoggers extends LitElement {
    jsonRpc = new JsonRpc(this);
    static styles = css`
      .logger-table {
        height: 100%;
        padding-bottom: 10px;
      }
    `

    static properties = {
        _loggers: { state: true }
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
    }

    render() {
        return html`<vaadin-grid .items="${this._loggers}" class="logger-table" theme="no-border">
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
                </vaadin-grid>`
    }
}
customElements.define('qwc-logging-manager-loggers', QwcLoggingManagerLoggers);
