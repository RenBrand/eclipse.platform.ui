#!/bin/bash


create_wiki_section () {
F_TARGET="$1" ; shift
F_SEARCH="$1" ; shift

NUM=1
curl -o search.csv 'https://bugs.eclipse.org/bugs/buglist.cgi?classification=Eclipse&component=Runtime&component=IDE&component=User%20Assistance&component=UI&list_id=5935738&product=Platform&query_format=advanced&target_milestone='$F_SEARCH'&query_based_on=&columnlist=bug_id%2Ctarget_milestone%2Cassigned_to%2Cbug_status%2Cresolution%2Cshort_desc%2Cbug_severity%2Cqa_contact&ctype=csv'
grep -v target_milestone search.csv >t1 ; mv t1 search.csv


echo "== $F_TARGET =="
echo ""
echo '{| class="wikitable" border="1"'
echo '|-'
echo '! !! Bug !! TM !! Sev !! Assign !! Status !! Title'

while read line; do
	BUG=$( echo $line | csvtool col 1 - )
	TARGET=$( echo $line | csvtool col 2 - )
	ASSIGNED_TO=$( echo $line | csvtool col 3 - )
	STATUS=$( echo $line | csvtool col 4 - )
	if [ RESOLVED = "$STATUS" -o VERIFIED = "$STATUS" ]; then
		STATUS=$( echo $line | csvtool col 5 - )
		PRE="<strike>"
		POST="</strike>"
	fi
	TITLE=$( echo $line | csvtool col 6 - )
	SEV=$( echo $line | csvtool col 7 - )
	QA=$( echo $line | csvtool col 8 - )
	if [ "platform-ui-triaged" = "$ASSIGNED_TO" -a ! -z "$QA" ]; then
		ASSIGNED_TO="$QA"
	fi

	echo '|-'
	echo "| $NUM || $PRE{{bug|$BUG}}$POST || $TARGET || $SEV || $ASSIGNED_TO || $STATUS || $PRE$TITLE$POST"
	BUG=""
	TARGET=""
	TITLE=""
	STATUS=""
	ASSIGNED_TO=""
	SEV=""
	PRE=""
	POST=""
	(( NUM = NUM + 1 ))
done < search.csv

echo '|-'
echo '|}'
echo ""
echo "Last Generated on '''$(date)'''"
echo ""

}

create_wiki_section "4.4 RC4" 4.4%20RC4
create_wiki_section "4.4 - unscheduled" 4.4

echo "= Completed milestones ="

echo ""

create_wiki_section 4.3.1 4.3.1
create_wiki_section 4.3.2 4.3.2
create_wiki_section "4.4 M1" 4.4%20M1
create_wiki_section "4.4 M2" 4.4%20M2
create_wiki_section "4.4 M3" 4.4%20M3
create_wiki_section "4.4 M4" 4.4%20M4
create_wiki_section "4.4 M5" 4.4%20M5
create_wiki_section "4.4 M6" 4.4%20M6
create_wiki_section "4.4 M7" 4.4%20M7
create_wiki_section "4.4 RC1" 4.4%20RC1
create_wiki_section "4.4 RC2" 4.4%20RC2
create_wiki_section "4.4 RC3" 4.4%20RC3

