declare module 'bootstrap/js/dist/carousel' {
  export default class Carousel {
    constructor(element: string | Element, options?: { interval?: boolean | number });
    static getOrCreateInstance(element: string | Element, options?: { interval?: boolean | number }): Carousel;
    dispose(): void;
  }
}
