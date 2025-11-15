export class DOMHelper {

    public updateText(elementId: string, text: string): void {
        const element = document.getElementById(elementId);
        if (element) {
            const valueElement = element.querySelector('.value');
            if (valueElement) {
                valueElement.textContent = text;
            } else {
                element.textContent = text;
            }
        }
    }

    public setVisibility(elementId: string, visible: boolean): void {
        const element = document.getElementById(elementId);
        if (element) {
            element.style.display = visible ? 'block' : 'none';
        }
    }

    /**
     * Add CSS class to element
     */
    public addClass(elementId: string, className: string): void {
        const element = document.getElementById(elementId);
        if (element && !element.classList.contains(className)) {
            element.classList.add(className);
        }
    }

    /**
     * Remove CSS class from element
     */
    public removeClass(elementId: string, className: string): void {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.remove(className);
        }
    }

    /**
     * Toggle CSS class on element
     */
    public toggleClass(elementId: string, className: string): void {
        const element = document.getElementById(elementId);
        if (element) {
            element.classList.toggle(className);
        }
    }

    /**
     * Get element by ID
     */
    public getElement<T extends HTMLElement>(elementId: string): T | null {
        return document.getElementById(elementId) as T;
    }

    /**
     * Query selector
     */
    public querySelector<T extends HTMLElement>(selector: string): T | null {
        return document.querySelector(selector) as T;
    }

    /**
     * Query selector all
     */
    public querySelectorAll<T extends HTMLElement>(selector: string): NodeListOf<T> {
        return document.querySelectorAll(selector) as NodeListOf<T>;
    }

    /**
     * Create element with attributes
     */
    public createElement<T extends HTMLElement>(
        tagName: string,
        attributes?: Record<string, string>,
        content?: string
    ): T {
        const element = document.createElement(tagName) as T;

        if (attributes) {
            Object.entries(attributes).forEach(([key, value]) => {
                element.setAttribute(key, value);
            });
        }

        if (content) {
            element.textContent = content;
        }

        return element;
    }
}