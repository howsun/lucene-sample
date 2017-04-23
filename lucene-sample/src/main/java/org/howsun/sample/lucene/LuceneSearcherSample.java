/**
 * 
 */
package org.howsun.sample.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;


/**
 * 说明:
 * 
 * @author howsun ->[howsun.zhang@gmail.com]
 * @version 1.0
 *
 * 2017年3月27日 下午3:12:49
 */
public class LuceneSearcherSample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Directory directory = FSDirectory.open(new File("e:/repository_lucene").toPath());
			IndexReader indexReader = DirectoryReader.open(directory);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			
			Analyzer analyzer = new IKAnalyzer();
			
			Query query = null;
			
			/*常用解析器
			QueryParser queryParser = new QueryParser("content", analyzer);
			query = queryParser.parse("美国");
			*/
			
			/*
			 * 匹配所有记录
			 * MatchAllDocsQuery matchAllDocsQuery = new MatchAllDocsQuery();
			 */
			
			/*多字段解析器*/
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer);
			query = parser.parse("曹操");
			
			/*数字字段查询*/
			NumericRangeQuery<Integer> numericRangeQuery = NumericRangeQuery.newIntRange("catgory", 2, 2, true, true);

			/*多条件组合查询*/
			BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
			booleanQuery.add(query, Occur.MUST);
			booleanQuery.add(numericRangeQuery, Occur.MUST);
			
			//indexSearcher.search(booleanQuery.build(), 10);
			
			TopDocs topDocs = indexSearcher.search(booleanQuery.build(), 10);
			ScoreDoc[] hits = topDocs.scoreDocs;
			for(ScoreDoc doc : hits){
				Document document = indexSearcher.doc(doc.doc);
				
				IndexableField titleField = document.getField("title");
				if(titleField != null){
					System.out.println(titleField.stringValue());
				}
				IndexableField contentField = document.getField("content");
				if(contentField != null){
					System.out.println(contentField.stringValue());
				}
				IndexableField hitField = document.getField("views");
				if(hitField != null){
					System.out.println("浏览：" + hitField.numericValue());
				}
				IndexableField catgoryField = document.getField("catgory");
				if(catgoryField != null){
					System.out.println("分类ID：" + catgoryField.numericValue());
				}
			}
			
			indexReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 高亮
	 * @param analyzer
	 * @param query
	 * @param doc
	 * @return
	 */
	private String toHighlighter(Analyzer analyzer, Query query, Document doc) {
		String field = "text";
		try {
			SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
			Highlighter highlighter = new Highlighter(simpleHtmlFormatter, new QueryScorer(query));
			TokenStream tokenStream1 = analyzer.tokenStream("text", new StringReader(doc.get(field)));
			String highlighterStr = highlighter.getBestFragment(tokenStream1, doc.get(field));
			return highlighterStr == null ? doc.get(field) : highlighterStr;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
