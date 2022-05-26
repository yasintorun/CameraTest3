import { FmtPanelRefType, PanelDirectionType } from "./fmtEnums";

export class FmtPanel{
    panelName!: string;
    qrCode!: string;
    cellData!: string[];
    panelRefType!: FmtPanelRefType;
    direction!: PanelDirectionType;
    map!: number[]; //0: startRow, 1: endRow, 2: startCol, 3: endCol
    format!: string; //okunacak format türü örn: ABCD, 0123456789 vs

    constructor() {

    }
}