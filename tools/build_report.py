"""Rebuild the PDF with ReportLab; Java compilation has no Python dependency.

Usage: python tools/build_report.py [--github-url https://github.com/owner/repo]
"""

import argparse
import re
from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak, Preformatted,
    Table, TableStyle, Flowable, KeepTogether,
)

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/java/edu/builder/travel"
INK = colors.HexColor("#17212B")
MUTED = colors.HexColor("#536170")
BLUE = colors.HexColor("#EAF0F5")
GRAY = colors.HexColor("#D9D9D9")
WIDTH = 504


def source(name):
    return (SRC / (name + ".java")).read_text()


def method(name, signature):
    text = source(name)
    start = text.index(signature)
    start = text.rfind("\n", 0, start) + 1
    opening = text.index("{", start)
    depth = 1
    pos = opening + 1
    while depth:
        depth += (text[pos] == "{") - (text[pos] == "}")
        pos += 1
    import textwrap
    return textwrap.dedent(text[start:pos])


class UML(Flowable):
    """Vector UML of the pattern roles; helper types are explained below it."""
    def __init__(self):
        super().__init__()
        self.width = WIDTH
        self.height = 465

    def draw(self):
        c = self.canv

        def box(x, y, w, h, title, lines=(), stereotype=None):
            c.setFillColor(colors.white)
            c.setStrokeColor(MUTED)
            c.setLineWidth(.7)
            c.rect(x, y, w, h, fill=1)
            header = 36 if stereotype else 26
            c.setFillColor(BLUE)
            c.rect(x, y+h-header, w, header, fill=1, stroke=0)
            c.setFillColor(INK)
            if stereotype:
                c.setFont("Helvetica-Oblique", 8)
                c.drawCentredString(x+w/2, y+h-12, stereotype)
            c.setFont("Helvetica-Bold", 10)
            c.drawCentredString(x+w/2, y+h-header+9, title)
            c.setStrokeColor(MUTED)
            c.line(x, y+h-header, x+w, y+h-header)
            c.setFont("Helvetica", 8.7)
            for i, line in enumerate(lines):
                c.drawString(x+9, y+h-header-17-i*15, line)

        def arrow(points, dashed=False, triangle=False):
            import math
            c.setStrokeColor(MUTED)
            c.setLineWidth(.85)
            c.setDash(4, 3) if dashed else c.setDash()
            p = c.beginPath()
            p.moveTo(*points[0])
            for point in points[1:]:
                p.lineTo(*point)
            c.drawPath(p)
            c.setDash()
            x, y = points[-1]
            x0, y0 = points[-2]
            angle = math.atan2(y-y0, x-x0)
            size = 8
            left = (x-size*math.cos(angle-.45), y-size*math.sin(angle-.45))
            right = (x-size*math.cos(angle+.45), y-size*math.sin(angle+.45))
            p = c.beginPath()
            p.moveTo(*left)
            p.lineTo(x, y)
            p.lineTo(*right)
            if triangle:
                p.close()
            c.setFillColor(colors.white)
            c.drawPath(p, fill=int(triangle))
            c.setFillColor(INK)

        box(317, 410, 183, 50, "Main", ["+ main(args: String[]): void"])
        box(0, 280, 283, 158, "TravelBuilder", [
            "+ setDestination(String): TravelBuilder",
            "+ setDays(int): TravelBuilder",
            "+ setAccommodation(Accommodation): TravelBuilder",
            "+ setTransport(Transport): TravelBuilder",
            "+ setActivities(List<String>): TravelBuilder",
        ], "<<interface>>")
        box(317, 300, 183, 87, "TravelDirector", [
            "+ makeCityBreak(b): TravelBuilder",
            "+ makeAdventureTrip(b): TravelBuilder",
            "b is a TravelBuilder parameter",
        ])
        arrow([(408, 410), (408, 387)], dashed=True)
        arrow([(317, 430), (298, 430), (298, 430), (283, 430)], dashed=True)
        arrow([(317, 327), (283, 327)], dashed=True)
        c.setFont("Helvetica-Oblique", 8)
        c.drawString(288, 334, "uses")

        box(0, 151, 247, 88, "TravelPackageObjectBuilder", [
            "- draft: TravelDraft",
            "+ getResult(): TravelPackage",
        ])
        box(279, 151, 221, 88, "ItineraryBuilder", [
            "- draft: TravelDraft",
            "+ getResult(): String",
        ])
        arrow([(123, 239), (123, 280)], dashed=True, triangle=True)
        arrow([(389, 239), (389, 263), (235, 263), (235, 280)], dashed=True, triangle=True)
        c.setFont("Helvetica-Oblique", 8)
        c.drawString(135, 251, "implements")
        box(0, 0, 247, 116, "TravelPackage", [
            "- destination: String {readOnly}",
            "- days: int {readOnly}",
            "- accommodation: Accommodation {readOnly}",
            "- transport: Transport {readOnly}",
            "- activities: List<String> {readOnly}",
        ], None)
        box(279, 37, 221, 65, "String", ["Human-readable itinerary"], "<<text representation>>")
        arrow([(123, 151), (123, 116)], dashed=True)
        arrow([(389, 151), (389, 102)], dashed=True)
        c.setFont("Helvetica-Oblique", 8)
        c.drawString(132, 128, "creates")
        c.drawString(398, 124, "creates")


def build(github_url):
    output = ROOT / "report" / "Builder_Pattern_Report.pdf"
    output.parent.mkdir(exist_ok=True)
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle("BodyText2", fontName="Helvetica", fontSize=10.5,
                              leading=15, textColor=INK, spaceAfter=8))
    styles.add(ParagraphStyle("HeadingA", fontName="Helvetica-Bold", fontSize=18,
                              leading=22, textColor=colors.black, spaceAfter=13))
    styles.add(ParagraphStyle("HeadingB", fontName="Helvetica-Bold", fontSize=12,
                              leading=15, textColor=colors.black, spaceBefore=10,
                              spaceAfter=6, keepWithNext=True))
    styles.add(ParagraphStyle("CodeText", fontName="Courier", fontSize=8.1,
                              leading=10.4, spaceBefore=4, spaceAfter=8))
    styles.add(ParagraphStyle("SmallText", fontName="Helvetica", fontSize=8.8,
                              leading=12, textColor=MUTED, spaceAfter=7))
    styles.add(ParagraphStyle("CellText", fontName="Helvetica", fontSize=9.3,
                              leading=12.5, textColor=INK))
    story = []

    def p(text, style="BodyText2"):
        story.append(Paragraph(text, styles[style]))

    def h(text):
        p(text, "HeadingB")

    def code(text, filename=None):
        if filename:
            p(filename + ".java", "SmallText")
        longest = max(map(len, text.splitlines()))
        if longest > 99:
            raise ValueError(f"Code line too long: {longest} chars")
        story.append(Preformatted(text, styles["CodeText"]))

    def page(title):
        story.append(PageBreak())
        p(title, "HeadingA")

    def table(rows, widths):
        data = [[Paragraph(escape(str(v)), styles["CellText"]) for v in row] for row in rows]
        t = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
        t.setStyle(TableStyle([
            ("BACKGROUND", (0, 0), (-1, 0), BLUE),
            ("GRID", (0, 0), (-1, -1), .5, GRAY),
            ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
            ("LEFTPADDING", (0, 0), (-1, -1), 9),
            ("RIGHTPADDING", (0, 0), (-1, -1), 9),
            ("TOPPADDING", (0, 0), (-1, -1), 8),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
        ]))
        story.append(t)
        story.append(Spacer(1, 8))

    p("Assignment 1", "SmallText")
    p("Builder Pattern for Travel Packages", "HeadingA")
    p("Java implementation and design report", "SmallText")
    h("Introduction")
    p("This project implements the Builder design pattern for a travel package. "
      "The same construction steps produce an immutable <b>TravelPackage</b> object "
      "and a readable itinerary as a <b>String</b>. A Director supplies two reusable "
      "configurations, while the client selects the representation by choosing a builder.")
    p("The example separates trip configuration from the format of the result. "
      "It demonstrates fluent method chaining, validation at the build boundary, "
      "defensive copying and dependencies expressed through an interface. Destinations "
      "and activities are demonstration data; the project does not book real travel.")
    h("Product and configurations")
    p("A package contains a destination, duration in days, accommodation, transport "
      "and an ordered activity list. Both presets execute the same five setters in "
      "the same order, including replacement of the complete activity list.")
    table([
        ["Attribute", "City break", "Adventure trip"],
        ["Destination", "Prague", "Almaty"],
        ["Duration", "3 days", "5 days"],
        ["Accommodation", "Hotel", "Guesthouse"],
        ["Transport", "Train", "Minibus"],
        ["Activities", "Old Town walking tour; Prague Castle visit", "Mountain hike; Lake visit"],
    ], [112, 196, 196])
    h("Construction roles")
    p("<b>Product:</b> TravelPackage. <b>Builder:</b> TravelBuilder. "
      "<b>Concrete builders:</b> TravelPackageObjectBuilder and ItineraryBuilder. "
      "<b>Director:</b> TravelDirector. <b>Client:</b> Main. The following diagram "
      "shows their relationships; later sections connect Clean Code principles to actual source excerpts.")

    page("UML class diagram")
    story.append(UML())
    story.append(Spacer(1, 14))
    p("<b>Notation:</b> + public, - private. A dashed arrow with a hollow triangle "
      "denotes interface implementation. Other dashed arrows denote dependencies "
      "or creation. {readOnly} denotes final product fields.", "SmallText")
    p("Each concrete builder owns a separate package-private <b>TravelDraft</b> "
      "containing its mutable state and shared validation. Accommodation and Transport "
      "are enums. These supporting types, routine getters and repeated setter "
      "signatures are omitted from the diagram. Both concrete builders implement "
      "all five steps with their own class as the return type.")
    p("<b>getResult()</b> belongs to each concrete builder because the return types "
      "are unrelated: TravelPackage and String. The Director uses only TravelBuilder "
      "and therefore never retrieves or casts either result.")

    page("Clean Code principles in the implementation")
    h("1 Meaningful names and named values")
    p("Names such as makeCityBreak, setDestination and getActivities describe their "
      "purpose. Preset data uses named constants; enum values express valid transport "
      "and accommodation choices. These names make the recipe understandable at the call site.")
    director = source("TravelDirector")
    constants = [line.strip() for line in director.splitlines()
                 if "private static final" in line and "List" not in line][:4]
    code("\n".join(constants), "TravelDirector")
    h("2 Small methods with one clear operation")
    p("Each setter changes one aspect of the draft and returns the same builder. "
      "The concrete return type preserves fluent chaining and still satisfies the interface.")
    code(method("TravelPackageObjectBuilder", "setDays("), "TravelPackageObjectBuilder")
    h("3 Single responsibility")
    p("TravelPackageObjectBuilder creates the domain object. ItineraryBuilder formats "
      "text. TravelDirector chooses preset values. A formatting change therefore "
      "does not require changing the Director or the immutable product.")
    code(method("TravelPackageObjectBuilder", "getResult("), "TravelPackageObjectBuilder")

    page("Clean Code principles continued")
    h("4 Depend on an abstraction")
    p("The Director accepts TravelBuilder, so one recipe works with either concrete "
      "builder. Adding another representation requires implementing the common steps; "
      "the existing recipe remains usable without inspecting concrete classes.")
    code(method("TravelDirector", "makeCityBreak("), "TravelDirector")
    h("5 Keep shared rules in one place")
    p("Both getResult methods call TravelDraft.validate(). Destination, duration, "
      "accommodation, transport and activity checks therefore use one implementation. "
      "This avoids one representation accepting a state that the other rejects.")
    validation = method("TravelDraft", "validate(")
    # Show the complete first checks, rather than a manufactured alternative implementation.
    first_checks = "\n".join(validation.splitlines()[:7])
    code(first_checks + "\n    // Remaining checks omitted in this excerpt.\n}", "TravelDraft")
    h("6 Encapsulate state and protect invariants")
    p("TravelPackage is final, its fields are private final and its constructor has "
      "package visibility. The constructor copies the activity list. Strings, enums "
      "and the day count are immutable values; the list cannot be changed through its getter.")
    code(method("TravelPackage", "TravelPackage("), "TravelPackage")

    page("Validation and execution")
    h("7 Make errors explicit")
    p("getResult validates the complete draft before returning a result. Invalid or "
      "missing required values produce IllegalStateException with a field-specific "
      "message. Main catches a deliberately invalid build and prints its explanation.")
    code(validation, "TravelDraft")
    h("Running the project")
    p("Install JDK 17 or newer. No Maven, Gradle or third-party Java libraries are required. "
      "From the project directory, run:")
    code("./run.sh\n./test.sh")
    p("The scripts compile the code with --release 17. Main demonstrates both "
      "presets in both representations, a custom fluent configuration and a validation error.")
    h("Example itinerary")
    demo = ROOT / "report" / "sample-output.txt"
    if demo.exists():
        lines = demo.read_text().splitlines()
        starts = [i for i, line in enumerate(lines) if line.strip() == "TRAVEL ITINERARY"]
        if starts:
            start = starts[0]
            excerpt = []
            for line in lines[start:start+10]:
                if not line.strip() and excerpt:
                    break
                excerpt.append(line)
            code("\n".join(excerpt))
        else:
            p("See report/sample-output.txt for the complete output captured from ./run.sh.")
    else:
        p("Run ./run.sh to print both representations of each preset.")

    page("Verification and conclusion")
    h("Verification results")
    tests = ROOT / "report" / "test-output.txt"
    if not tests.exists():
        raise RuntimeError("Capture actual test results before generating the report")
    output_text = tests.read_text().strip()
    p("The executable test runner was compiled and run with the supplied test.sh script. "
      "The following is the recorded result:")
    code(output_text)
    table([
        ["Property", "What the executable checks establish"],
        ["Two representations", "Both Director presets produce matching destination, duration, accommodation, transport and activities."],
        ["Fluent interface", "Every construction step returns the same concrete builder instance."],
        ["Validation", "Both builders reject missing or invalid required state with clear exceptions."],
        ["Immutability", "Changing the input list or reusing the builder does not change an already built product; its activity list rejects mutation."],
        ["Reuse", "Applying another preset replaces the old activity list and the remaining fields."],
    ], [116, 388])
    h("Conclusion")
    p("The implementation meets the Builder requirements by sharing construction "
      "steps across two result types. The Director stores reusable recipes, each "
      "builder produces its own representation, and the immutable product is isolated "
      "from subsequent draft changes. Shared validation keeps the accepted input "
      "rules consistent. The additional classes and setter delegation add some code, "
      "but make the two representations and their responsibilities explicit.")
    h("Repository and submission")
    if github_url:
        p('GitHub repository: <link href="' + escape(github_url, {'"': '&quot;'}) +
          '" color="#245A81">' + escape(github_url) + '</link>')
    else:
        p("<b>GitHub link pending.</b> Publication is deferred. The local project "
          "includes incremental Git commits; add the real repository URL to this "
          "report after the code is pushed and before submitting the PDF to Moodle.")
    p("The source package includes a short README, the Java implementation, a test "
      "runner, this report, editable UML source and Russian notes for the in-class defense.")

    def footer(c, doc):
        c.saveState()
        c.setFont("Helvetica", 8)
        c.setFillColor(MUTED)
        c.drawString(54, 27, "Assignment 1  |  Builder Pattern  |  Travel Packages")
        c.drawRightString(558, 27, str(doc.page))
        c.restoreState()

    doc = SimpleDocTemplate(str(output), pagesize=letter,
                           leftMargin=54, rightMargin=54,
                           topMargin=43, bottomMargin=48,
                           title="Builder Pattern for Travel Packages",
                           author="", subject="Assignment 1 Java Builder Pattern")
    doc.build(story, onFirstPage=footer, onLaterPages=footer)
    print(output)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--github-url")
    args = parser.parse_args()
    if args.github_url and not re.fullmatch(r"https://github\.com/[\w.-]+/[\w.-]+/?", args.github_url):
        parser.error("Use a full https://github.com/owner/repository URL")
    build(args.github_url)
