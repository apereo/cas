export class Util {
  static isEmpty(obj: any): boolean {
    return !obj || Object.keys(obj).length === 0;
  }
}
