export class ImageLoader {

    public loadImage(url: string): Promise<HTMLImageElement> {
        return new Promise((resolve, reject) => {
            const img = new Image();

            img.onload = () => resolve(img);
            img.onerror = () => reject(new Error(`Failed to load image: ${url}`));

            img.src = url;
        });
    }

    /**
     * Load image from File object
     */
    public loadFileAsImage(file: File): Promise<HTMLImageElement> {
        return new Promise((resolve, reject) => {
            if (!file.type.startsWith('image/')) {
                reject(new Error('File is not an image'));
                return;
            }

            const reader = new FileReader();

            reader.onload = (e) => {
                const img = new Image();

                img.onload = () => resolve(img);
                img.onerror = () => reject(new Error('Failed to load image from file'));

                img.src = e.target?.result as string;
            };

            reader.onerror = () => reject(new Error('Failed to read file'));
            reader.readAsDataURL(file);
        });
    }

    /**
     * Convert image to base64
     */
    public imageToBase64(img: HTMLImageElement): string {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;

        const ctx = canvas.getContext('2d');
        if (!ctx) {
            throw new Error('Failed to get canvas context');
        }

        ctx.drawImage(img, 0, 0);
        return canvas.toDataURL('image/png');
    }

    /**
     * Create image from base64
     */
    public base64ToImage(base64: string): Promise<HTMLImageElement> {
        return new Promise((resolve, reject) => {
            const img = new Image();

            img.onload = () => resolve(img);
            img.onerror = () => reject(new Error('Failed to load image from base64'));

            img.src = base64;
        });
    }

    /**
     * Resize image
     */
    public resizeImage(
        img: HTMLImageElement,
        maxWidth: number,
        maxHeight: number
    ): HTMLCanvasElement {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) {
            throw new Error('Failed to get canvas context');
        }

        let width = img.width;
        let height = img.height;

        // Calculate new dimensions
        if (width > maxWidth || height > maxHeight) {
            const ratio = Math.min(maxWidth / width, maxHeight / height);
            width *= ratio;
            height *= ratio;
        }

        canvas.width = width;
        canvas.height = height;

        ctx.drawImage(img, 0, 0, width, height);

        return canvas;
    }

    /**
     * Get image dimensions
     */
    public getImageDimensions(url: string): Promise<{ width: number; height: number }> {
        return new Promise((resolve, reject) => {
            const img = new Image();

            img.onload = () => {
                resolve({
                    width: img.width,
                    height: img.height
                });
            };

            img.onerror = () => reject(new Error('Failed to load image'));
            img.src = url;
        });
    }
}