import { DOMHelper } from './helpers/dom';
import { ImageLoader } from './helpers/imageLoader';

class EdgeDetectionViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private imageLoader: ImageLoader;
    private domHelper: DOMHelper;
    private currentMode: string = 'Canny';
    private animationId: number | null = null;
    private lastFrameTime: number = 0;
    private frameCount: number = 0;
    private fps: number = 0;

    constructor() {
        this.canvas = document.getElementById('imageCanvas') as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.imageLoader = new ImageLoader();
        this.domHelper = new DOMHelper();

        this.initializeCanvas();
        this.setupEventListeners();
        this.startFPSCounter();
        this.loadSampleImage();
    }

    private initializeCanvas(): void {
        this.canvas.width = 800;
        this.canvas.height = 600;
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
    }

    private setupEventListeners(): void {
        // Load sample button
        document.getElementById('loadSample')?.addEventListener('click', () => {
            this.loadSampleImage();
        });

        // Filter buttons
        document.getElementById('applyCannyFilter')?.addEventListener('click', () => {
            this.applyFilter('Canny');
        });

        document.getElementById('applySobelFilter')?.addEventListener('click', () => {
            this.applyFilter('Sobel');
        });

        document.getElementById('applyGrayscaleFilter')?.addEventListener('click', () => {
            this.applyFilter('Grayscale');
        });

        // Upload image button
        document.getElementById('uploadImage')?.addEventListener('click', () => {
            document.getElementById('fileInput')?.click();
        });

        // File input change
        document.getElementById('fileInput')?.addEventListener('change', (event) => {
            const input = event.target as HTMLInputElement;
            if (input.files && input.files[0]) {
                this.loadUserImage(input.files[0]);
            }
        });
    }

    private async loadSampleImage(): Promise<void> {
        const startTime = performance.now();

        try {
            const image = await this.imageLoader.loadImage('assets/sample_output.png');
            this.displayImage(image);

            const processingTime = performance.now() - startTime;
            this.updateStats(image.width, image.height, processingTime);

            // Simulate edge detection on sample
            this.simulateEdgeDetection(image);
        } catch (error) {
            console.error('Failed to load sample image:', error);
            this.showErrorMessage('Failed to load sample image');
        }
    }

    private async loadUserImage(file: File): Promise<void> {
        const startTime = performance.now();

        try {
            const image = await this.imageLoader.loadFileAsImage(file);
            this.displayImage(image);

            const processingTime = performance.now() - startTime;
            const fileSize = (file.size / 1024).toFixed(2);

            this.updateStats(image.width, image.height, processingTime);
            this.domHelper.updateText('imageSize', `${fileSize} KB`);

            this.simulateEdgeDetection(image);
        } catch (error) {
            console.error('Failed to load user image:', error);
            this.showErrorMessage('Failed to load image');
        }
    }

    private displayImage(image: HTMLImageElement): void {
        // Calculate aspect ratio
        const aspectRatio = image.width / image.height;
        let drawWidth = this.canvas.width;
        let drawHeight = this.canvas.height;

        if (aspectRatio > this.canvas.width / this.canvas.height) {
            drawHeight = this.canvas.width / aspectRatio;
        } else {
            drawWidth = this.canvas.height * aspectRatio;
        }

        const x = (this.canvas.width - drawWidth) / 2;
        const y = (this.canvas.height - drawHeight) / 2;

        // Clear canvas
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // Draw image
        this.ctx.drawImage(image, x, y, drawWidth, drawHeight);
    }

    private simulateEdgeDetection(image: HTMLImageElement): void {
        const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);

        switch (this.currentMode) {
            case 'Canny':
                this.applyCannySimulation(imageData);
                break;
            case 'Sobel':
                this.applySobelSimulation(imageData);
                break;
            case 'Grayscale':
                this.applyGrayscaleSimulation(imageData);
                break;
        }

        this.ctx.putImageData(imageData, 0, 0);
    }

    private applyCannySimulation(imageData: ImageData): void {
        const data = imageData.data;

        for (let i = 0; i < data.length; i += 4) {
            // Convert to grayscale
            const gray = data[i] * 0.299 + data[i + 1] * 0.587 + data[i + 2] * 0.114;

            // Simple edge detection simulation
            const threshold = 128;
            const edge = gray > threshold ? 255 : 0;

            data[i] = edge;
            data[i + 1] = edge;
            data[i + 2] = edge;
        }
    }

    private applySobelSimulation(imageData: ImageData): void {
        const data = imageData.data;
        const width = imageData.width;
        const height = imageData.height;

        // Create grayscale copy
        const gray = new Uint8ClampedArray(width * height);
        for (let i = 0, j = 0; i < data.length; i += 4, j++) {
            gray[j] = data[i] * 0.299 + data[i + 1] * 0.587 + data[i + 2] * 0.114;
        }

        // Apply Sobel operator (simplified)
        for (let y = 1; y < height - 1; y++) {
            for (let x = 1; x < width - 1; x++) {
                const idx = (y * width + x) * 4;
                const grayIdx = y * width + x;

                // Sobel X
                const sobelX =
                    gray[grayIdx - width - 1] * -1 +
                    gray[grayIdx - width + 1] * 1 +
                    gray[grayIdx - 1] * -2 +
                    gray[grayIdx + 1] * 2 +
                    gray[grayIdx + width - 1] * -1 +
                    gray[grayIdx + width + 1] * 1;

                // Sobel Y
                const sobelY =
                    gray[grayIdx - width - 1] * -1 +
                    gray[grayIdx - width] * -2 +
                    gray[grayIdx - width + 1] * -1 +
                    gray[grayIdx + width - 1] * 1 +
                    gray[grayIdx + width] * 2 +
                    gray[grayIdx + width + 1] * 1;

                const magnitude = Math.sqrt(sobelX * sobelX + sobelY * sobelY);
                const edge = Math.min(255, magnitude);

                data[idx] = edge;
                data[idx + 1] = edge;
                data[idx + 2] = edge;
            }
        }
    }

    private applyGrayscaleSimulation(imageData: ImageData): void {
        const data = imageData.data;

        for (let i = 0; i < data.length; i += 4) {
            const gray = data[i] * 0.299 + data[i + 1] * 0.587 + data[i + 2] * 0.114;
            data[i] = gray;
            data[i + 1] = gray;
            data[i + 2] = gray;
        }
    }

    private applyFilter(mode: string): void {
        this.currentMode = mode;
        this.domHelper.updateText('currentMode', mode);

        // Re-apply filter to current canvas content
        const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);

        // Create temporary image from canvas
                const tempCanvas = document.createElement('canvas');
                tempCanvas.width = this.canvas.width;
                tempCanvas.height = this.canvas.height;
                const tempCtx = tempCanvas.getContext('2d')!;
                tempCtx.putImageData(imageData, 0, 0);

                const img = new Image();
                img.onload = () => {
                    this.displayImage(img);
                    this.simulateEdgeDetection(img);
                };
                img.src = tempCanvas.toDataURL();
            }

            private updateStats(width: number, height: number, processingTime: number): void {
                this.domHelper.updateText('resolution', `${width}x${height}`);
                this.domHelper.updateText('processingTime', `${processingTime.toFixed(2)}ms`);

                const now = new Date();
                const timeStr = now.toTimeString().split(' ')[0];
                this.domHelper.updateText('timestamp', timeStr);
            }

            private startFPSCounter(): void {
                const updateFPS = () => {
                    const currentTime = performance.now();
                    if (this.lastFrameTime !== 0) {
                        const delta = currentTime - this.lastFrameTime;
                        this.fps = 1000 / delta;
                        this.frameCount++;

                        if (this.frameCount % 30 === 0) {
                            this.domHelper.updateText('fps', this.fps.toFixed(1));
                        }
                    }
                    this.lastFrameTime = currentTime;
                    this.animationId = requestAnimationFrame(updateFPS);
                };
                updateFPS();
            }

            private showErrorMessage(message: string): void {
                this.ctx.fillStyle = '#000';
                this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
                this.ctx.fillStyle = '#fff';
                this.ctx.font = '20px Arial';
                this.ctx.textAlign = 'center';
                this.ctx.fillText(message, this.canvas.width / 2, this.canvas.height / 2);
            }

            public destroy(): void {
                if (this.animationId) {
                    cancelAnimationFrame(this.animationId);
                }
            }
        }

        // Initialize the viewer when DOM is loaded
        document.addEventListener('DOMContentLoaded', () => {
            const viewer = new EdgeDetectionViewer();

            // Cleanup on page unload
            window.addEventListener('beforeunload', () => {
                viewer.destroy();
            });
        });