import '@vaadin/icon';

// Registers a custom Vaadin icon set named "custom" so that `new Icon("custom:rocket")` works.
// This demonstrates that the dynamic menu can render fully custom icon families, not just the
// built-in Vaadin icons or FontAwesome.
const template = document.createElement('template');
template.innerHTML = `
  <vaadin-iconset name="custom" size="24">
    <svg xmlns="http://www.w3.org/2000/svg">
      <defs>
        <g id="custom:rocket">
          <path d="M12 2c3.5 2.5 5 6 5 10l-2.2 2.2H9.2L7 12C7 8 8.5 4.5 12 2zm0 6.2a1.6 1.6 0 100 3.2 1.6 1.6 0 000-3.2zM9 16h6l-1.2 4.5L12 22l-1.8-1.5L9 16z"/>
        </g>
        <g id="custom:flow">
          <path d="M5 4h6v6H5V4zm8 10h6v6h-6v-6zM8 10v4h5m0-4h3v4h-3"/>
        </g>
      </defs>
    </svg>
  </vaadin-iconset>
`;
document.head.appendChild(template.content);
