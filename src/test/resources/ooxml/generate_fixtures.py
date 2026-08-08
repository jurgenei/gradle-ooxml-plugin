from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile

BASE = Path(__file__).parent

PNG_BYTES = bytes([
    137, 80, 78, 71, 13, 10, 26, 10,
    0, 0, 0, 13, 73, 72, 68, 82,
    0, 0, 0, 1, 0, 0, 0, 1,
    8, 2, 0, 0, 0, 144, 119, 83,
    222, 0, 0, 0, 12, 73, 68, 65,
    84, 8, 153, 99, 248, 15, 4, 0,
    9, 251, 3, 253, 167, 145, 160, 244,
    0, 0, 0, 0, 73, 69, 78, 68,
    174, 66, 96, 130,
])


def write_zip(file_name: str, entries: list[tuple[str, bytes | str]]) -> None:
    path = BASE / file_name
    with ZipFile(path, "w", ZIP_DEFLATED) as zip_file:
        for name, content in entries:
            zip_file.writestr(name, content)


def main() -> None:
    BASE.mkdir(parents=True, exist_ok=True)

    write_zip(
        "sample_v3.docx",
        [
            (
                "[Content_Types].xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'>"
                "<Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/>"
                "<Default Extension='xml' ContentType='application/xml'/>"
                "<Default Extension='png' ContentType='image/png'/>"
                "</Types>",
            ),
            (
                "_rels/.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument' Target='word/document.xml'/>"
                "</Relationships>",
            ),
            (
                "word/document.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<w:document xmlns:w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'>"
                "<w:body>"
                "<w:p><w:r><w:t>Hello from DOCX</w:t></w:r></w:p>"
                "<w:p><w:r><w:t>Second paragraph</w:t></w:r></w:p>"
                "<w:p><w:pPr><w:numPr><w:ilvl w:val='0'/><w:numId w:val='1'/></w:numPr></w:pPr><w:r><w:t>List item 1</w:t></w:r></w:p>"
                "<w:p><w:pPr><w:numPr><w:ilvl w:val='0'/><w:numId w:val='1'/></w:numPr></w:pPr><w:r><w:t>List item 2</w:t></w:r></w:p>"
                "<w:p><w:hyperlink r:id='rId2'><w:r><w:t>Docx Link</w:t></w:r></w:hyperlink></w:p>"
                "<w:p><w:hyperlink w:anchor='bookmark-1'><w:r><w:t>Docx Internal Ref</w:t></w:r></w:hyperlink></w:p>"
                "<w:bookmarkStart w:id='0' w:name='bookmark-1'/>"
                "<w:p><w:r><w:drawing><wp:inline xmlns:wp='http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing'><wp:docPr id='11' name='Docx Shape A'/></wp:inline></w:drawing></w:r></w:p>"
                "<w:tbl>"
                "<w:tr><w:tc><w:p><w:r><w:t>R1C1</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>R1C2</w:t></w:r></w:p></w:tc></w:tr>"
                "<w:tr><w:tc><w:p><w:r><w:t>R2C1</w:t></w:r></w:p></w:tc><w:tc><w:p><w:r><w:t>R2C2</w:t></w:r></w:p></w:tc></w:tr>"
                "</w:tbl>"
                "</w:body>"
                "</w:document>",
            ),
            (
                "word/_rels/document.xml.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId2' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink' Target='https://example.com/docx' TargetMode='External'/>"
                "</Relationships>",
            ),
            ("word/media/image1.png", PNG_BYTES),
        ],
    )

    write_zip(
        "sample.pptx",
        [
            (
                "[Content_Types].xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'>"
                "<Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/>"
                "<Default Extension='xml' ContentType='application/xml'/>"
                "<Default Extension='png' ContentType='image/png'/>"
                "</Types>",
            ),
            (
                "_rels/.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument' Target='ppt/presentation.xml'/>"
                "</Relationships>",
            ),
            (
                "ppt/presentation.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<p:presentation xmlns:p='http://schemas.openxmlformats.org/presentationml/2006/main'/>",
            ),
            (
                "ppt/slides/slide1.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<p:sld xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main' xmlns:p='http://schemas.openxmlformats.org/presentationml/2006/main'>"
                "<p:cSld><p:spTree>"
                "<p:sp><p:nvSpPr><p:cNvPr id='201' name='Ppt Shape A'/></p:nvSpPr><p:txBody>"
                "<a:p><a:r><a:t>Slide 1 Title</a:t></a:r></a:p>"
                "<a:p><a:pPr><a:buChar char='*'/></a:pPr><a:r><a:t>Bullet one</a:t></a:r></a:p>"
                "<a:p><a:pPr><a:buChar char='*'/></a:pPr><a:r><a:t>Bullet two</a:t></a:r></a:p>"
                "</p:txBody></p:sp>"
                "<p:sp><p:nvSpPr><p:cNvPr id='202' name='Ppt Shape B'/></p:nvSpPr><p:txBody><a:p><a:r><a:t>Connected Node</a:t></a:r></a:p></p:txBody></p:sp>"
                "<p:cxnSp><p:nvCxnSpPr><p:cNvPr id='203' name='Ppt Connector'/><p:cNvCxnSpPr><a:stCxn id='201' idx='0'/><a:endCxn id='202' idx='0'/></p:cNvCxnSpPr></p:nvCxnSpPr></p:cxnSp>"
                "<p:graphicFrame><a:graphic><a:graphicData>"
                "<a:tbl>"
                "<a:tr><a:tc><a:txBody><a:p><a:r><a:t>P11</a:t></a:r></a:p></a:txBody></a:tc><a:tc><a:txBody><a:p><a:r><a:t>P12</a:t></a:r></a:p></a:txBody></a:tc></a:tr>"
                "<a:tr><a:tc><a:txBody><a:p><a:r><a:t>P21</a:t></a:r></a:p></a:txBody></a:tc><a:tc><a:txBody><a:p><a:r><a:t>P22</a:t></a:r></a:p></a:txBody></a:tc></a:tr>"
                "</a:tbl>"
                "</a:graphicData></a:graphic></p:graphicFrame>"
                "<p:sp><p:txBody><a:p><a:r><a:rPr><a:hlinkClick xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships' r:id='rId2'/></a:rPr><a:t>Slide Link</a:t></a:r></a:p></p:txBody></p:sp>"
                "</p:spTree></p:cSld>"
                "</p:sld>",
            ),
            (
                "ppt/slides/_rels/slide1.xml.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId2' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink' Target='https://example.com/pptx' TargetMode='External'/>"
                "</Relationships>",
            ),
            ("ppt/media/image1.png", PNG_BYTES),
        ],
    )

    write_zip(
        "sample.xlsx",
        [
            (
                "[Content_Types].xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Types xmlns='http://schemas.openxmlformats.org/package/2006/content-types'>"
                "<Default Extension='rels' ContentType='application/vnd.openxmlformats-package.relationships+xml'/>"
                "<Default Extension='xml' ContentType='application/xml'/>"
                "<Default Extension='png' ContentType='image/png'/>"
                "</Types>",
            ),
            (
                "_rels/.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId1' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument' Target='xl/workbook.xml'/>"
                "</Relationships>",
            ),
            (
                "xl/workbook.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<workbook xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main'>"
                "<sheets><sheet name='Sheet1' sheetId='1' r:id='rId1' xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'/></sheets>"
                "</workbook>",
            ),
            (
                "xl/sharedStrings.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<sst xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' count='4' uniqueCount='4'>"
                "<si><t>Cell From XLSX</t></si>"
                "<si><t>Header</t></si>"
                "<si><t>R2C1</t></si>"
                "<si><t>R2C2</t></si>"
                "</sst>",
            ),
            (
                "xl/worksheets/sheet1.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<worksheet xmlns='http://schemas.openxmlformats.org/spreadsheetml/2006/main' xmlns:r='http://schemas.openxmlformats.org/officeDocument/2006/relationships'>"
                "<sheetData>"
                "<row r='1'><c r='A1' t='s'><v>0</v></c><c r='B1' t='s'><v>1</v></c></row>"
                "<row r='2'><c r='A2' t='s'><v>2</v></c><c r='B2' t='s'><v>3</v></c></row>"
                "</sheetData>"
                "<hyperlinks>"
                "<hyperlink ref='A1' r:id='rId2' display='XLSX Link'/>"
                "<hyperlink ref='B1' location='Sheet1!A2' display='Local Ref'/>"
                "</hyperlinks>"
                "<drawing r:id='rId3'/>"
                "</worksheet>",
            ),
            (
                "xl/worksheets/_rels/sheet1.xml.rels",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<Relationships xmlns='http://schemas.openxmlformats.org/package/2006/relationships'>"
                "<Relationship Id='rId2' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink' Target='https://example.com/xlsx' TargetMode='External'/>"
                "<Relationship Id='rId3' Type='http://schemas.openxmlformats.org/officeDocument/2006/relationships/drawing' Target='../drawings/drawing1.xml'/>"
                "</Relationships>",
            ),
            (
                "xl/drawings/drawing1.xml",
                "<?xml version='1.0' encoding='UTF-8'?>"
                "<xdr:wsDr xmlns:xdr='http://schemas.openxmlformats.org/drawingml/2006/spreadsheetDrawing' xmlns:a='http://schemas.openxmlformats.org/drawingml/2006/main'>"
                "<xdr:twoCellAnchor><xdr:sp><xdr:nvSpPr><xdr:cNvPr id='301' name='Sheet Shape A'/></xdr:nvSpPr></xdr:sp></xdr:twoCellAnchor>"
                "<xdr:twoCellAnchor><xdr:sp><xdr:nvSpPr><xdr:cNvPr id='302' name='Sheet Shape B'/></xdr:nvSpPr></xdr:sp></xdr:twoCellAnchor>"
                "<xdr:twoCellAnchor><xdr:cxnSp><xdr:nvCxnSpPr><xdr:cNvPr id='303' name='Sheet Connector'/><xdr:cNvCxnSpPr><a:stCxn id='301' idx='0'/><a:endCxn id='302' idx='0'/></xdr:cNvCxnSpPr></xdr:nvCxnSpPr></xdr:cxnSp></xdr:twoCellAnchor>"
                "</xdr:wsDr>",
            ),
            ("xl/media/image1.png", PNG_BYTES),
        ],
    )


if __name__ == "__main__":
    main()

