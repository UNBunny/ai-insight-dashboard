declare module "*.module.css" {
  const classes: { [key: string]: string };
  export default classes;
}

// Declare module for components
declare module "../../components/ui/table" {
  export const Table: React.FC<React.HTMLAttributes<HTMLTableElement>>;
  export const TableHeader: React.FC<React.HTMLAttributes<HTMLTableSectionElement>>;
  export const TableBody: React.FC<React.HTMLAttributes<HTMLTableSectionElement>>;
  export const TableFooter: React.FC<React.HTMLAttributes<HTMLTableSectionElement>>;
  export const TableRow: React.FC<React.HTMLAttributes<HTMLTableRowElement>>;
  export const TableHead: React.FC<React.ThHTMLAttributes<HTMLTableCellElement>>;
  export const TableCell: React.FC<React.TdHTMLAttributes<HTMLTableCellElement>>;
  export const TableCaption: React.FC<React.HTMLAttributes<HTMLTableCaptionElement>>;
}

declare module "../../components/ui/dialog" {
  export const Dialog: React.FC<any>;
  export const DialogContent: React.FC<any>;
  export const DialogHeader: React.FC<React.HTMLAttributes<HTMLDivElement>>;
  export const DialogFooter: React.FC<React.HTMLAttributes<HTMLDivElement>>;
  export const DialogTitle: React.FC<any>;
  export const DialogDescription: React.FC<any>;
  export const DialogTrigger: React.FC<any>;
  export const DialogClose: React.FC<any>;
}

declare module "../../components/ui/alert" {
  export const Alert: React.FC<any>;
  export const AlertTitle: React.FC<React.HTMLAttributes<HTMLHeadingElement>>;
  export const AlertDescription: React.FC<React.HTMLAttributes<HTMLParagraphElement>>;
}
