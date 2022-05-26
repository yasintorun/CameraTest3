import { FmtRefType } from "./fmtEnums";

export class Fmt {
    public examName!: string;
    public sessionName!: string;
    public hasBrans!: string;
    public bransId!: number;
    public static CELLDATARAW: Map<string, number>;

    public radius!: string;
    public unitSize!: number;

    public orderCorner!: number[];
    public refType!: FmtRefType;
    private raw!: string;

    public leftRectWidth!: number;
    public dotRectHeight!: number;
    public studentAnswers!: string;

    private dimensions!: number[];
    private columns!: number;
    private rows!: number;
    
    public questionAnswersVal!: number[];
    public questionAnswers!: string[];
    
    public answerKeys!: string;
    public isMissingMarks: boolean = false;

    get Raw(): string {return this.raw;}
    set Raw(raw:string) {this.raw = raw;}
    
    get Dimensions(): number[] {return this.dimensions;}
    set Dimensions(dimensions: number[]) {this.dimensions = dimensions;}

    get Columns(): number {return this.columns;}
    set Columns(columns: number) {this.columns = columns;}
    
    get Rows(): number {return this.rows;}
    set Rows(rows: number) {this.rows = rows;}

    public isVertical!: boolean;

    //constructors
    public constructor (raw: string) {
        this.isMissingMarks = false;
        this.createCellDataRAW()
        // this.raw = raw;
    }

    private createCellDataRAW(): void
    {
        Fmt.CELLDATARAW = new Map<string, number>();
        Fmt.CELLDATARAW.set('A', 1);
        Fmt.CELLDATARAW.set('B', 2);
        Fmt.CELLDATARAW.set('C', 3);
        Fmt.CELLDATARAW.set('D', 4);
        Fmt.CELLDATARAW.set('E', 5);
        Fmt.CELLDATARAW.set('F', 6);
        Fmt.CELLDATARAW.set('G', 7);
        Fmt.CELLDATARAW.set('H', 8);
        Fmt.CELLDATARAW.set('I', 9);
        Fmt.CELLDATARAW.set('J', 10);
        Fmt.CELLDATARAW.set('K', 11);
        Fmt.CELLDATARAW.set('L', 12);
        Fmt.CELLDATARAW.set('M', 13);
        Fmt.CELLDATARAW.set('N', 14);
        Fmt.CELLDATARAW.set('O', 15);
        Fmt.CELLDATARAW.set('P', 16);
        Fmt.CELLDATARAW.set('R', 17);
        Fmt.CELLDATARAW.set('S', 18);
        Fmt.CELLDATARAW.set('T', 19);
        Fmt.CELLDATARAW.set('U', 20);
        Fmt.CELLDATARAW.set('V', 21);
        Fmt.CELLDATARAW.set('Y', 22);
        Fmt.CELLDATARAW.set('Z', 23);

        Fmt.CELLDATARAW.set('Ç', 24);
        Fmt.CELLDATARAW.set('Ğ', 25);
        Fmt.CELLDATARAW.set('İ', 26);
        Fmt.CELLDATARAW.set('Ö', 27);
        Fmt.CELLDATARAW.set('Ş', 28);
        Fmt.CELLDATARAW.set('Ü', 29);
        Fmt.CELLDATARAW.set(' ', 30);
        Fmt.CELLDATARAW.set('-', 40);

        Fmt.CELLDATARAW.set('0', 100);
        Fmt.CELLDATARAW.set('1', 101);
        Fmt.CELLDATARAW.set('2', 102);
        Fmt.CELLDATARAW.set('3', 103);
        Fmt.CELLDATARAW.set('4', 104);
        Fmt.CELLDATARAW.set('5', 105);
        Fmt.CELLDATARAW.set('6', 106);
        Fmt.CELLDATARAW.set('7', 107);
        Fmt.CELLDATARAW.set('8', 108);
        Fmt.CELLDATARAW.set('9', 109);
    }
    
}